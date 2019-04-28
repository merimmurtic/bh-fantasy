package com.fifa.wolrdcup.workers;

import com.fifa.wolrdcup.model.League;
import com.fifa.wolrdcup.model.Match;
import com.fifa.wolrdcup.model.Round;
import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.repository.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransferMarktWorker extends ProcessWorker {

    private static final String BASE_URL = "https://www.transfermarkt.com";

    private static Logger logger = LoggerFactory.getLogger(TransferMarktWorker.class);

    private static final String FORMATION_REGEX = "(?:\\d-)+\\d";

    private final String transfermarktUrl;

    public TransferMarktWorker(StadiumRepository stadiumRepository,
                        GoalRepository goalRepository,
                        MatchRepository matchRepository,
                        TeamRepository teamRepository,
                        RoundRepository roundRepository,
                        LeagueRepository leagueRepository,
                        PlayerRepository playerRepository,
                        String transfermarktUrl) {
        super(stadiumRepository, goalRepository, matchRepository,
                teamRepository, roundRepository, leagueRepository, playerRepository);

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
            String formation = parseFormation(lineupElement);

            if(formation != null) {
                Elements playerElements = lineupElement.select(".aufstellung-spieler-container a");

                for (Element playerElement : playerElements) {
                    processPlayerUrl(playerElement.text(), playerElement.attr("href"), team);
                }
            } else {
                Elements playerElements = lineupElement.select(".spielprofil_tooltip");

                for(Element playerElement : playerElements) {
                    processPlayerUrl(playerElement.text(), playerElement.attr("href"), team);
                }
            }
        } catch (Exception e) {
            logger.error("Error while processing lineup.", e);
        }
    }

    private void processPlayerUrl(String playerName, String playerInfoUrl, Team team) {
        String[] nameParts = playerName.split(" ", 2);

        Long transferMarktId = getTransferMarktId(playerInfoUrl);

        if(nameParts.length == 1) {
            processPlayer(null, playerName, team, transferMarktId);
        } else {
            processPlayer(nameParts[0], nameParts[1], team, transferMarktId);
        }
    }

    private Long getTransferMarktId(String playerInfoUrl) {
        String[] parts = playerInfoUrl.split("/");

        return Long.parseLong(parts[parts.length - 1]);
    }

    private String parseFormation(Element lineupElement) {
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
            return matcher.group(0);
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
