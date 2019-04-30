package com.fifa.wolrdcup.workers;

import com.fifa.wolrdcup.model.*;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.model.players.Player.Position;
import com.fifa.wolrdcup.model.players.Unknown;
import com.fifa.wolrdcup.repository.*;
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

    private final String transfermarktUrl;

    public TransferMarktWorker(StadiumRepository stadiumRepository,
                        GoalRepository goalRepository,
                        MatchRepository matchRepository,
                        TeamRepository teamRepository,
                        RoundRepository roundRepository,
                        LeagueRepository leagueRepository,
                        PlayerRepository playerRepository,
                        LineupRepository lineupRepository,
                        String transfermarktUrl) {
        super(stadiumRepository, goalRepository, matchRepository,
                teamRepository, roundRepository, leagueRepository, playerRepository, lineupRepository);

        this.transfermarktUrl = transfermarktUrl;
    }

    public void process() throws Exception {
        Document document = Jsoup.parse(new URL(BASE_URL.concat(transfermarktUrl)), 10000);

        String leagueName = document.select(".spielername-profil").text();
        String leagueLevel = document.select(
                ".box-personeninfos tr").first().select("td").text();

        League league = new League();
        league.setName(leagueName);

        leagueRepository.save(league);

        Elements matchDays = document.select(".row .large-6 .box");

        processRounds(matchDays, league);
    }

    private void processRounds(Elements matchDays, League league) {
        for(Element matchDayElement : matchDays) {
            Round round = new Round();

            String matchDay = matchDayElement.select(".table-header").first().text();

            round.setName(matchDay);
            round.setLeague(league);

            roundRepository.save(round);

            logger.info("Round '{}' saved.", round.getName());

            Elements matchElements = matchDayElement.select("tr");

            processMatches(matchElements, round, league);

            break;
        }
    }

    private void processMatches(Elements matchElements, Round round, League league) {
        for(Element matchElement : matchElements) {
            Elements elements = matchElement.select("td");

            if (elements.size() == 7) {
                Match match = new Match();

                match.setTeam1(processTeam(processTeamMap(elements.get(2).select("a").text()), league));
                match.setTeam2(processTeam(processTeamMap(elements.get(6).select("a").text()), league));

                Element matchDetailsElement = elements.get(4).selectFirst("a");

                String[] scores = matchDetailsElement.text().split(":");

                try {
                    match.setScore1(Integer.parseInt(scores[0]));
                    match.setScore2(Integer.parseInt(scores[1]));
                } catch (NumberFormatException e) {
                    // Match is not played
                }

                match.setRound(round);

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
                processLineup(lineupDocuments.first(), match, match.getTeam1());
                processLineup(lineupDocuments.last(), match, match.getTeam2());
            }
        } catch (IOException e) {
            logger.error("Error while processing match {}.", matchUrl);
        }
    }

    private void processLineup(Element lineupElement, Match match, Team team) {
        try {
            Lineup.Formation formation = parseFormation(lineupElement);

            Lineup lineup = new Lineup();
            lineup.setMatch(match);
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
                        logger.error("Error while parsing number on dress: ", e);
                    }

                    Element playerElement = substitutionElement.select("tr").last().selectFirst("a");

                    Player player = processPlayerUrl(
                            playerElement.text(), playerElement.attr("href"), team, numberOnDress);

                    lineup.getAvailableSupstitutions().add(player);
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
        } catch (Exception e) {
            logger.error("Error while processing lineup.", e);
        }
    }

    private Player processPlayerUrl(String playerName, String playerInfoUrl, Team team, Integer numberOnDress) {
        Long transferMarktId = getTransferMarktId(playerInfoUrl);

        Optional<Player> optionalPlayer = playerRepository.findByTransferMarktId(transferMarktId);

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

        String[] nameParts = playerName.split(" ", 2);

        if(player == null) {
            player = new Unknown();
        }

        player.setTransferMarktId(transferMarktId);

        if(nameParts.length == 1) {
            player.setLastName(playerName);
        } else {
            player.setFirstName(nameParts[0]);
            player.setLastName(nameParts[1]);
        }

        return processPlayer(player, team);
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

    private Map<String, String> processTeamMap(String teamName) {
        Map<String, String> teamMap = new HashMap<>();
        teamMap.put("name", teamName);
        teamMap.put("code", teamName);

        return teamMap;
    }
}
