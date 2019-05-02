package com.fifa.wolrdcup.workers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifa.wolrdcup.model.*;
import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.model.league.RegularLeague;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import javax.transaction.Transactional;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class WorldCupWorker extends ProcessWorker {

    private static Logger logger = LoggerFactory.getLogger(WorldCupWorker.class);

    public WorldCupWorker(
            StadiumRepository stadiumRepository,
            GoalRepository goalRepository,
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            RoundRepository roundRepository,
            LeagueRepository leagueRepository,
            PlayerRepository playerRepository) {
        super(stadiumRepository, goalRepository, matchRepository,
                teamRepository, roundRepository, leagueRepository,
                playerRepository, null, null, null);
    }

    @SuppressWarnings("unchecked")
    public Long process() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            RegularLeague league = new RegularLeague();

            HashMap<String, Object> map = objectMapper.readValue(
                    ResourceUtils.getFile("classpath:worldcup.json"),
                    new TypeReference<HashMap<String,Object>>() {});

            league.setName((String) map.get("name"));
            leagueRepository.save(league);

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
    @Transactional
    public void processMatches(List<HashMap<String, Object>> matches, Round round, League league) {
        for (HashMap<String, Object> matchMap : matches) {
            Match match = new Match();
            match.setRound(round);


            match.setTeam1(processTeam((HashMap<String, String>) matchMap.get("team1"), league));
            match.setTeam2(processTeam((HashMap<String, String>) matchMap.get("team2"), league));


            // There is matches without stadium, you need to check if it exist first
            if(matchMap.containsKey("stadium")) {
                match.setStadium(processStadium((HashMap<String, String>) matchMap.get("stadium")));
            }

            // By using getOrDefault you can get value always, there is no need to check if key exist,
            // if key does not exist, value you provided as defaultValue will be returned
            if(matchMap.getOrDefault("score1et", null) != null){
                match.setScore1((Integer) matchMap.get("score1et"));
            } else if(matchMap.containsKey("score1")){
                match.setScore1((Integer) matchMap.get("score1"));
            }

            if(matchMap.getOrDefault("score2et", null) != null){
                match.setScore2((Integer) matchMap.get("score2et"));
            } else if(matchMap.containsKey("score2")){
                match.setScore2((Integer) matchMap.get("score2"));
            }

            matchRepository.save(match);

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




