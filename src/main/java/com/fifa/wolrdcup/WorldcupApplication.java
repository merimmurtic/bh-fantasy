package com.fifa.wolrdcup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fifa.wolrdcup.model.*;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.model.players.Unknown;
import com.fifa.wolrdcup.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class WorldcupApplication {

    private static Logger logger = LoggerFactory.getLogger(WorldcupApplication.class);

    private final StadiumRepository stadiumRepository;

    private final LeagueRepository leagueRepository;

    private final RoundRepository roundRepository;

    private final TeamRepository teamRepository;

    private final MatchRepository matchRepository;

    private final PlayerRepository playerRepository;

    private final GoalRepository goalRepository;

    public WorldcupApplication(
            StadiumRepository stadiumRepository,
            GoalRepository goalRepository,
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            RoundRepository roundRepository,
            LeagueRepository leagueRepository,
            PlayerRepository playerRepository) {
        this.leagueRepository = leagueRepository;
        this.roundRepository = roundRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.goalRepository = goalRepository;
        this.stadiumRepository = stadiumRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(WorldcupApplication.class, args);
    }

    @Bean
    InitializingBean sendDatabase() {
        return () -> {
            populateDatabase();
        };
    }

    @SuppressWarnings("unchecked")
    private void populateDatabase() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            League league = new League();

            HashMap<String, Object> map = objectMapper.readValue(
                    ResourceUtils.getFile("classpath:worldcup.json"),
                    new TypeReference<HashMap<String,Object>>() {});

            league.setName((String) map.get("name"));
            leagueRepository.save(league);

            processRounds((List<HashMap<String, Object>> ) map.get("rounds"), league);
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                logger.warn("World cup file not found");
            } else {
                throw e;
            }
        }
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
            Match match = new Match();
            match.setRound(round);
            match.setTeam1(processTeam((HashMap<String, String>) matchMap.get("team1"), league));
            match.setTeam2(processTeam((HashMap<String, String>) matchMap.get("team2"), league));

            // There is matches without stadium, you need to check if it exist first
            if(matchMap.containsKey("stadium")) {
                match.setStadium(processStadium((HashMap<String, String>) matchMap.get("stadium")));
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

    private Team processTeam(HashMap<String, String> teamMap, League league) {
        Team team = new Team();
        team.setCode(teamMap.get("code"));
        team.setName(teamMap.get("name"));
        team.getLeagues().add(league);

        Optional<Team> existingTeam = teamRepository.findByCode(team.getCode());

        //if(!existingTeam.isPresent()) {
          //  return teamRepository.save(team);
        //} else {
          //  return existingTeam.get();
        //}
        return existingTeam.orElseGet(() -> teamRepository.save(team));

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

    private Player processPlayer(String firstName, Team team) {
        Player player = new Unknown();
        player.setFirstName(firstName);
        player.setTeam(team);

        Optional<Player> existingPlayer = playerRepository.findByTeamAndFirstNameAndLastName(team, firstName, null);

        return  existingPlayer.orElseGet(() -> playerRepository.save(player));
    }

    // You don't need match inside this method, reference with match is done outside this method
    private Stadium processStadium(HashMap<String, String> stadiumMap) {
        // Use constructor if you have one :)
        Stadium stadium = new Stadium(stadiumMap.get("name"), stadiumMap.get("key"));

        Optional<Stadium> existingStadium = stadiumRepository.findByKey(stadium.getKey());

        return existingStadium.orElseGet(() -> stadiumRepository.save(stadium));
    }
}
