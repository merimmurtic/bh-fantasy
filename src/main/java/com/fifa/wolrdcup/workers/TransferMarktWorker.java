package com.fifa.wolrdcup.workers;

import com.fifa.wolrdcup.model.*;
import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.model.league.RegularLeague;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.model.players.Player.Position;
import com.fifa.wolrdcup.model.players.Unknown;
import com.fifa.wolrdcup.repository.*;
import com.fifa.wolrdcup.service.PlayerService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransferMarktWorker extends ProcessWorker {

    private static final String BASE_URL = "https://www.transfermarkt.com";

    private static Logger logger = LoggerFactory.getLogger(TransferMarktWorker.class);

    private static final String FORMATION_REGEX = "(?:\\d-)+\\d";

    private static final String POSITION_REGEX = "(?:Position: (.*))";

    private static final String DATE_REGEX = "(\\d\\d.\\d\\d.\\d\\d\\d\\d)";

    private static final String BACKGROUND_POSITIONS_REGEX = "(-\\d*)px";

    private final String transfermarktUrl;

    public TransferMarktWorker(StadiumRepository stadiumRepository,
                        GoalRepository goalRepository,
                        MatchRepository matchRepository,
                        TeamRepository teamRepository,
                        RoundRepository roundRepository,
                        LeagueRepository leagueRepository,
                        PlayerService playerService,
                        LineupRepository lineupRepository,
                        SubstitutionRepository substitutionRepository,
                        CardRepository cardRepository,
                        MissedPenaltyRepository missedPenaltyRepository,
                        String transfermarktUrl) {
        super(stadiumRepository, goalRepository, matchRepository,
                teamRepository, roundRepository, leagueRepository,
                playerService, lineupRepository, substitutionRepository, cardRepository, missedPenaltyRepository);

        this.transfermarktUrl = transfermarktUrl;
    }

    public Long process() throws Exception {
        Document document = Jsoup.parse(new URL(BASE_URL.concat(transfermarktUrl)), 10000);

        String leagueName = document.select(".spielername-profil").text();
        String leagueLevel = document.select(
                ".box-personeninfos tr").first().select("td").text();

        Optional<League> optionalLeague = leagueRepository.findByName(leagueName);

        RegularLeague league = null;

        if(optionalLeague.isPresent()) {
            league = (RegularLeague) optionalLeague.get();
        } else {
            league = new RegularLeague();
            league.setName(leagueName);

            leagueRepository.save(league);
        }

        Elements matchDays = document.select(".row .large-6 .box");

        processRounds(matchDays, league);

        return league.getId();
    }

    private void processRounds(Elements matchDays, League league) {
        for(Element matchDayElement : matchDays) {
            Round round = null;

            String matchDay = matchDayElement.select(".table-header").first().text();

            Optional<Round> optionalRound = roundRepository.findByLeagueIdAndName(league.getId(), matchDay);

            if(optionalRound.isPresent()) {
                round = optionalRound.get();
            } else {
                round = new Round();
                round.setName(matchDay);
                round.setLeague(league);

                roundRepository.save(round);

                logger.info("Round '{}' saved.", round.getName());
            }

            Elements matchElements = matchDayElement.select("tr");

            processMatches(matchElements, round, league);
        }
    }

    private void processMatches(Elements matchElements, Round round, League league) {
        for(Element matchElement : matchElements) {
            Elements elements = matchElement.select("td");

            if (elements.size() == 7) {
                Element matchDetailsElement = elements.get(4).selectFirst("a");

                Match match = null;

                Long transferMarktId = null;

                if(matchDetailsElement.attr("id") != null) {
                    try {
                        transferMarktId = Long.parseLong(matchDetailsElement.attr("id"));
                    } catch (NumberFormatException e) {
                        //
                    }
                }

                if(transferMarktId != null) {
                    Optional<Match> optionalMatch = matchRepository.findByTransfermarktId(transferMarktId);

                    if(optionalMatch.isPresent()) {
                        match = optionalMatch.get();
                    }
                }

                if(match == null) {
                    match = new Match();
                    match.setTransfermarktId(transferMarktId);

                    String profilePictureTeam1 = elements.get(3).select("img")
                            .attr("src").replace("tiny", "normal");

                    String profilePictureTeam2 = elements.get(5).select("img")
                            .attr("src").replace("tiny", "normal");

                    match.setTeam1(processTeam(processTeamMap(elements.get(2).select("a").text(), profilePictureTeam1), league));
                    match.setTeam2(processTeam(processTeamMap(elements.get(6).select("a").text(), profilePictureTeam2), league));
                    match.setRound(round);
                } else if(match.getLineup1() != null) {
                    //TODO: Find better way to figure out if match has updates
                    continue;
                }

                String[] scores = matchDetailsElement.text().split(":");

                try {
                    match.setScore1(Integer.parseInt(scores[0]));
                    match.setScore2(Integer.parseInt(scores[1]));
                } catch (NumberFormatException e) {
                    // Match is not played
                }

                matchRepository.save(match);

                if(match.getScore1() != null) {
                    processMatch(matchDetailsElement.attr("href"), match);
                }
            }
        }
    }

    private void processMatch(String matchUrl, Match match) {
        try {
            Document document = Jsoup.parse(new URL(BASE_URL.concat(matchUrl)), 10000);

            Elements lineupDocuments = document.select("#main .box .large-6");

            if(lineupDocuments.size() == 2) {
                match.setLineup1(processLineup(lineupDocuments.first(), match.getTeam1()));
                match.setLineup2(processLineup(lineupDocuments.last(), match.getTeam2()));
            }

            match.setStadium(processStadium(document.selectFirst("#main .box .sb-zusatzinfos")));

            matchRepository.save(match);

            Elements goalElements = document.select("#sb-tore ul li");

            processGoals(goalElements, match);

            Elements substitutionElements = document.select("#sb-wechsel ul li");

            processSubstitutions(substitutionElements, match);

            Elements cardElements = document.select("#sb-karten ul li");

            processCards(cardElements, match);

            Elements missedPenaltyELements = document.select("#sb-verschossene ul li");

            processMissedPenalties(missedPenaltyELements, match);
        } catch (IOException e) {
            logger.error("Error while processing match {}.", matchUrl);
        }
    }

    private Stadium processStadium(Element stadiumElement) {
        Elements elements = stadiumElement.select("a");

        String stadionName = null;

        for(Element element : elements) {
            if(element.attr("href") != null && element.attr("href").startsWith("/stadion")) {
                stadionName = element.text();
            }
        }

        if(stadionName != null) {
            Optional<Stadium> optionalStadium = stadiumRepository.findByKey(stadionName);

            if(optionalStadium.isPresent()) {
                return optionalStadium.get();
            }

            Stadium stadium = new Stadium();
            stadium.setKey(stadionName);
            stadium.setName(stadionName);

            return stadiumRepository.save(stadium);
        }

        return null;
    }

    private void processMissedPenalties(Elements missedPenaltyElements, Match match) {
        List<MissedPenalty> missedPenalties = new ArrayList<>();

        for(Element misserPenaltyElement : missedPenaltyElements) {
            MissedPenalty missedPenalty = new MissedPenalty();

            missedPenalty.setMinute(calculateMinute(misserPenaltyElement.selectFirst(".sb-sprite-uhr-klein")));

            Team team = match.getTeam1();
            Team otherTeam = match.getTeam2();

            if(misserPenaltyElement.hasClass("sb-aktion-gast")) {
                team = match.getTeam2();
                otherTeam = match.getTeam1();
            }

            Elements playerElements = misserPenaltyElement.select(".sb-aktion-aktion span");

            for(Element playerElementSpan : playerElements) {
                Element playerElement = playerElementSpan.selectFirst("a");

                if(playerElement == null) {
                    continue;
                }

                Player player = new Unknown();
                player.setTransferMarktId(Long.parseLong(playerElement.attr("id")));

                populateFirstAndLastName(playerElement.text(), player);

                if(playerElementSpan.text().contains("Missed")) {
                    continue;
                } else if(playerElementSpan.text().contains("Saved")) {
                    missedPenalty.setSavedBy(playerService.processPlayer(player, otherTeam));
                } else {
                    missedPenalty.setPlayer(playerService.processPlayer(player, team));
                }
            }

            missedPenalty.setMatch(match);

            missedPenalties.add(missedPenalty);
        }

        missedPenaltyRepository.saveAll(missedPenalties);
    }

    private void processCards(Elements cardElements, Match match) {
        List<Card> cards = new ArrayList<>();

        for(Element cardElement : cardElements) {
            Card card = new Card();

            card.setMinute(calculateMinute(cardElement.selectFirst(".sb-sprite-uhr-klein")));

            Team team = match.getTeam1();

            if(cardElement.hasClass("sb-aktion-gast")) {
                team = match.getTeam2();
            }

            Element playerElement = cardElement.selectFirst(".sb-aktion-aktion a");

            Player player = new Unknown();
            player.setTransferMarktId(Long.parseLong(playerElement.attr("id")));

            populateFirstAndLastName(playerElement.text(), player);

            card.setPlayer(playerService.processPlayer(player, team));

            Card.CardType cardType = Card.CardType.YELLOW;

            if(cardElement.selectFirst(".sb-rot") != null) {
                cardType = Card.CardType.RED;
            }

            card.setCardType(cardType);
            card.setMatch(match);

            cards.add(card);
        }

        cardRepository.saveAll(cards);
    }

    private void processSubstitutions(Elements substitutionElements, Match match) {
        List<Substitution> substitutions = new ArrayList<>();

        for(Element substitutionElement : substitutionElements) {
            Substitution substitution = new Substitution();

            Team team = match.getTeam1();

            Lineup lineup = match.getLineup1();

            if(substitutionElement.hasClass("sb-aktion-gast")) {
                lineup = match.getLineup2();

                team = match.getTeam2();
            }

            substitution.setLineup(lineup);
            substitution.setMinute(calculateMinute(substitutionElement.selectFirst(".sb-sprite-uhr-klein")));

            Element playerInElement = substitutionElement.selectFirst(".sb-aktion-wechsel-ein a");

            try {
                if(playerInElement != null) {
                    Player playerIn = new Unknown();
                    playerIn.setTransferMarktId(Long.parseLong(playerInElement.attr("id")));

                    populateFirstAndLastName(playerInElement.text(), playerIn);

                    substitution.setPlayer(playerService.processPlayer(playerIn, team));
                }

                Element playerOutElement = substitutionElement.selectFirst(".sb-aktion-wechsel-aus a");

                Player playerOut = new Unknown();
                playerOut.setTransferMarktId(Long.parseLong(playerOutElement.attr("id")));

                populateFirstAndLastName(playerOutElement.text(), playerOut);

                substitution.setSubstitutePlayer(playerService.processPlayer(playerOut, team));

                substitutions.add(substitution);
            } catch (Exception e) {
                logger.error("error", e);
            }
        }

        substitutionRepository.saveAll(substitutions);
    }

    private void processGoals(Elements goalElements, Match match) {
        List<Goal> goals = new ArrayList<>();

        for(Element goalElement : goalElements) {
            Goal goal = new Goal();
            goal.setMatch(match);

            Team team = match.getTeam1();

            if(goalElement.hasClass("sb-aktion-gast")) {
                team = match.getTeam2();
            }

            Elements playerElements = goalElement.select(".sb-aktion-aktion a");

            if(playerElements.size() > 0) {
                Element playerElement = playerElements.get(0);

                Long transferMarktId = Long.parseLong(playerElement.attr("id"));

                Player player = new Unknown();
                player.setTransferMarktId(transferMarktId);

                populateFirstAndLastName(playerElement.text(), player);

                player = playerService.processPlayer(player, team);

                goal.setPlayer(player);
            }

            if(playerElements.size() > 1) {
                Element playerElement = playerElements.get(1);

                Long transferMarktId = Long.parseLong(playerElement.attr("id"));

                Player player = new Unknown();
                player.setTransferMarktId(transferMarktId);

                populateFirstAndLastName(playerElement.text(), player);

                player = playerService.processPlayer(player, team);

                goal.setAssist(player);
            }

            goal.setMinute(calculateMinute(goalElement.selectFirst(".sb-sprite-uhr-klein")));

            String[] scoreParts = goalElement.selectFirst(".sb-aktion-spielstand").text().split(":");

            if(scoreParts.length == 2) {
                goal.setScore1(Integer.parseInt(scoreParts[0]));
                goal.setScore2(Integer.parseInt(scoreParts[1]));
            }

            goal.setPenalty(goalElement.text().contains("Penalty"));
            goal.setOwnGoal(goalElement.text().contains("Own-goal"));

            goals.add(goal);
        }

        goalRepository.saveAll(goals);
    }

    private Integer calculateMinute(Element minuteElement) {
        String minuteStyle = minuteElement.attr("style");

        final Pattern pattern = Pattern.compile(BACKGROUND_POSITIONS_REGEX, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(minuteStyle);

        int x, y;

        if(matcher.find()) {
            x = Integer.parseInt(matcher.group(1));
        } else {
            return null;
        }

        if(matcher.find()) {
            y = Integer.parseInt(matcher.group(1));
        } else {
            return null;
        }

        int minute = (1 + (Math.abs(x) / 36)) + 10*((Math.abs(y) / 36));

        try {
            minute += Integer.parseInt(minuteElement.text());
        } catch (NumberFormatException e) {
            //
        }

        return minute;
    }

    private void populateFirstAndLastName(String fullName, Player player) {
        String[] nameParts = fullName.split(" ", 2);

        if(nameParts.length == 1) {
            player.setLastName(nameParts[0]);
        } else {
            player.setFirstName(nameParts[0]);
            player.setLastName(nameParts[1]);
        }
    }

    private Lineup processLineup(Element lineupElement, Team team) {
        try {
            Lineup.Formation formation = parseFormation(lineupElement);

            Lineup lineup = new Lineup();
            lineup.setFormation(formation);

            lineupRepository.save(lineup);

            if(formation != null) {
                Elements playerElements = lineupElement.select(".aufstellung-spieler-container");

                for (Element playerElement : playerElements) {
                    Element playerElementA = playerElement.selectFirst("a");

                    Integer numberOnDress = null;

                    try {
                        numberOnDress = Integer.parseInt(
                                playerElement.selectFirst(".aufstellung-rueckennummer-box").text());
                    } catch (Exception e) {
                        logger.error("Error while parsing number on dress: ", e);
                    }

                    Player player = processPlayerUrl(
                            playerElementA.text(), playerElementA.attr("href"), team, numberOnDress);

                    lineup.getStartingPlayers().add(player);

                    if(playerElement.selectFirst(".kapitaenicon-formation") != null) {
                        lineup.setCapiten(player);
                    }
                }

                Elements substitutionElements = lineupElement.select(".aufstellung-ersatzbank-box tr");

                for(Element substitutionElement : substitutionElements) {
                    if(substitutionElement.selectFirst(".spielprofil_tooltip") == null)  {
                        continue;
                    }

                    Integer numberOnDress = null;

                    try {
                        numberOnDress = Integer.parseInt(
                                substitutionElement.selectFirst("td").text());
                    } catch (Exception e) {
                        //
                    }

                    Element playerElement = substitutionElement.select("tr").last().selectFirst("a");

                    Player player = processPlayerUrl(
                            playerElement.text(), playerElement.attr("href"), team, numberOnDress);

                    lineup.getAvailableSubstitutions().add(player);
                }
            } else {
                Elements playerElements = lineupElement.select(".spielprofil_tooltip");

                for(Element playerElement : playerElements) {
                    Player player = processPlayerUrl(
                            playerElement.text(), playerElement.attr("href"), team, null);

                    lineup.getStartingPlayers().add(player);
                }
            }

            lineupRepository.save(lineup);

            return lineup;
        } catch (Exception e) {
            logger.error("Error while processing lineup.", e);
        }

        return null;
    }

    private Player processPlayerUrl(String playerName, String playerInfoUrl, Team team, Integer numberOnDress) {
        Long transferMarktId = getTransferMarktId(playerInfoUrl);

        Optional<Player> optionalPlayer = playerService.getPlayerRepository().findByTransferMarktId(transferMarktId);

        if(optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();

            if(player.getPosition() != null && player.getTransferMarktId() != null) {
                return player;
            }
        }

        Player player = null;

        try {
            Document document = Jsoup.connect(BASE_URL.concat("/spieler/_profilTooltip"))
                    .data("spieler_id", transferMarktId.toString()).timeout(10000).post();

            playerName = document.select(".spielername-kurzprofil a").text();

            Position position = parsePosition(document.selectFirst(".kurzprofil-infos"));

            player = Player.getInstance(position);
            player.setPosition(position);

            String marketValueRaw = document.selectFirst(".kurzprofil-marktwert")
                    .text().replace("Market Value: ", "");

            LocalDate dateBirth = parseDate(document.selectFirst(".kurzprofil-infos-text")
                    .selectFirst("br").previousSibling().toString());

            String profilePicture = document.selectFirst(".bilderrahmen").attr("src");

            player.setProfilePicture(profilePicture);
            player.setBirthDate(dateBirth != null ? Date.valueOf(dateBirth) : null);
            player.setMarketValueRaw(marketValueRaw);
            player.setNumberoOnDress(numberOnDress);
        } catch (IOException e) {
            logger.error("Error while loading player info.", e);
        }

        if(player == null) {
            player = new Unknown();
        }

        player.setTransferMarktId(transferMarktId);

        populateFirstAndLastName(playerName, player);

        return playerService.processPlayer(player, team);
    }

    private Long getTransferMarktId(String playerInfoUrl) {
        String[] parts = playerInfoUrl.split("/");

        return Long.parseLong(parts[parts.length - 1]);
    }

    private Position parsePosition(Element element) {
        String posititonText = element.select("br").last().previousSibling().toString();

        final Pattern pattern = Pattern.compile(POSITION_REGEX, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(posititonText);

        if(matcher.find()) {
            String position = matcher.group(1).trim();

            return Position.getPosition(position);
        } else {
            return Position.UNKNOWN;
        }
    }

    private LocalDate parseDate(String dateText) {
        final Pattern pattern = Pattern.compile(DATE_REGEX, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(dateText);

        if(matcher.find()) {
            String date = matcher.group(0);

            return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } else {
            return null;
        }
    }

    private Lineup.Formation parseFormation(Element lineupElement) {
        Element row = lineupElement.selectFirst(".row");

        if(row == null) {
            return null;
        }

        Element div = row.selectFirst("div");

        if(div == null) {
            return null;
        }

        String text = div.text();

        final Pattern pattern = Pattern.compile(FORMATION_REGEX, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(text);

        if(matcher.find()) {
            String formation = matcher.group(0);

            return Lineup.Formation.valueOf("F_" + formation.replace("-", "_"));
        } else {
            return null;
        }
    }

    private Map<String, String> processTeamMap(String teamName, String profilePicture) {
        Map<String, String> teamMap = new HashMap<>();
        teamMap.put("name", teamName);
        teamMap.put("code", teamName);
        teamMap.put("picture", profilePicture);

        return teamMap;
    }
}
