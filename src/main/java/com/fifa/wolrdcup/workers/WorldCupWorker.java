package com.fifa.wolrdcup.workers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifa.wolrdcup.model.*;
import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.model.league.RegularLeague;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.repository.*;
import com.fifa.wolrdcup.service.LeagueService;
import com.fifa.wolrdcup.service.MatchService;
import com.fifa.wolrdcup.service.PlayerService;
import com.fifa.wolrdcup.service.TeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class WorldCupWorker extends ProcessWorker {

    private static Logger logger = LoggerFactory.getLogger(WorldCupWorker.class);

    private final String season;

    public WorldCupWorker(
            StadiumRepository stadiumRepository,
            GoalRepository goalRepository,
            MatchService matchService,
            TeamService teamService,
            RoundRepository roundRepository,
            LeagueService leagueService,
            PlayerService playerService, String season) {
        super(stadiumRepository, goalRepository, matchService,
                teamService, roundRepository, leagueService,
                playerService, null, null,
                null, null);

        this.season = season;
    }

    @SuppressWarnings("unchecked")
    public Long process() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            HashMap<String, Object> map = objectMapper.readValue(
                    ResourceUtils.getFile("classpath:worldcup.json"),
                    new TypeReference<HashMap<String,Object>>() {});

            String leagueName = (String) map.get("name");

            Optional<RegularLeague> optionalLeague = leagueService.getRegularLeague(leagueName, season);

            if(optionalLeague.isPresent()) {
                logger.info("{} for season {} is already processed!", leagueName, season);
                return optionalLeague.get().getId();
            }

            logger.info("Processing league {}.", leagueName);

            RegularLeague league = leagueService.createRegularLeague(leagueName, season);

            processRounds((List<HashMap<String, Object>>) map.get("rounds"), league);

            return league.getId();
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                logger.warn("World cup file not found");
            } else {
                throw e;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private void processRounds(List<HashMap<String, Object>> rounds, League league) {
        for (HashMap<String, Object> roundMap : rounds) {
            Round round = new Round();
            round.setName((String) roundMap.get("name"));
            round.setLeague(league);

            roundRepository.save(round);

            processMatches((List<HashMap<String, Object>>) roundMap.get("matches"), round, league);

        }
    }

    @SuppressWarnings("unchecked")
    private void processMatches(List<HashMap<String, Object>> matches, Round round, League league) {
        for (HashMap<String, Object> matchMap : matches) {
            Team team1 = processTeam((HashMap<String, String>) matchMap.get("team1"), league);
            Team team2 = processTeam((HashMap<String, String>) matchMap.get("team2"), league);

            Stadium stadium = null;

            // There is matches without stadium, you need to check if it exist first
            if(matchMap.containsKey("stadium")) {
                stadium = processStadium((HashMap<String, String>) matchMap.get("stadium"));
            }

            Integer score1 = null;
            Integer score2 = null;

            // By using getOrDefault you can get value always, there is no need to check if key exist,
            // if key does not exist, value you provided as defaultValue will be returned
            if(matchMap.getOrDefault("score1et", null) != null){
                score1 = (Integer) matchMap.get("score1et");
            } else if(matchMap.containsKey("score1")){
                score1 = (Integer) matchMap.get("score1");
            }

            if(matchMap.getOrDefault("score2et", null) != null){
                score2 = (Integer) matchMap.get("score2et");
            } else if(matchMap.containsKey("score2")){
                score2 = (Integer) matchMap.get("score2");
            }

            Match match = null;

            try {
                match = matchService.createMatch(
                        team1, team2, null, score1, score2, stadium);
            } catch (Exception e) {
                logger.error("");
            }

            match = matchService.addRound(match.getId(), round);

            if(matchMap.containsKey("goals1")) {
                processGoals((List<HashMap<String, Object>>) matchMap.get("goals1"),
                        match, match.getTeam1(), match.getTeam2());
            }

            if(matchMap.containsKey("goals2")) {
                processGoals((List<HashMap<String, Object>>) matchMap.get("goals2"),
                        match, match.getTeam2(), match.getTeam1());
            }

        }
    }

    private void processGoals(List<HashMap<String, Object>> goals, Match match, Team team, Team teamAgainstPlayed) {
        for (HashMap<String, Object> goalMap : goals) {
            Boolean ownGoal = (Boolean) goalMap.getOrDefault("owngoal", false);

            Goal goal = new Goal();
            goal.setScore1((Integer) goalMap.get("score1"));
            goal.setScore2((Integer) goalMap.get("score2"));

            goal.setMinute((Integer) goalMap.get("minute"));
            goal.setOwnGoal(ownGoal);
            goal.setPenalty((Boolean) goalMap.getOrDefault("penalty", false));
            goal.setMatch(match);

            goal.setPlayer(processPlayer((String) goalMap.get("name"), ownGoal ? teamAgainstPlayed : team));

            goalRepository.save(goal);
        }
    }

    private Player processPlayer(String playerName, Team team) {
        String[] nameParts = playerName.split(" ", 2);

        if(nameParts.length == 1) {
            return processPlayer(null, playerName, team);
        } else {
            return processPlayer(nameParts[0], nameParts[1], team);
        }
    }


    // You don't need match inside this method, reference with match is done outside this method
    private Stadium processStadium(HashMap<String, String> stadiumMap) {
        // Use constructor if you have one :)
        Stadium stadium = new Stadium(stadiumMap.get("name"), stadiumMap.get("key"));

        Optional<Stadium> existingStadium = stadiumRepository.findByKey(stadium.getKey());

        return existingStadium.orElseGet(() -> stadiumRepository.save(stadium));
    }



}




