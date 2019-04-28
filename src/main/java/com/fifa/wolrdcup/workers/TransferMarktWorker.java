package com.fifa.wolrdcup.workers;

import com.fifa.wolrdcup.model.League;
import com.fifa.wolrdcup.model.Match;
import com.fifa.wolrdcup.model.Round;
import com.fifa.wolrdcup.repository.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TransferMarktWorker extends ProcessWorker {

    private static Logger logger = LoggerFactory.getLogger(TransferMarktWorker.class);

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
        Document document = Jsoup.parse(new URL(transfermarktUrl), 10000);

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

                String[] scores = elements.get(4).text().split(":");

                try {
                    match.setScore1(Integer.parseInt(scores[0]));
                    match.setScore2(Integer.parseInt(scores[1]));
                } catch (NumberFormatException e) {
                    // Match is not played
                }

                match.setRound(round);

                matchRepository.save(match);
            }
        }
    }

    private Map<String, String> processTeamMap(String teamName) {
        Map<String, String> teamMap = new HashMap<>();
        teamMap.put("name", teamName);
        teamMap.put("code", teamName);

        return teamMap;
    }
}
