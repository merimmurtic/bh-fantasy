package com.fifa.wolrdcup;

import com.fifa.wolrdcup.repository.*;
import com.fifa.wolrdcup.workers.TransferMarktWorker;
import com.fifa.wolrdcup.workers.WorldCupWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
            startWorkers();
        };
    }

    private void startWorkers() throws Exception {
        //new WorldCupWorker(
        //        stadiumRepository, goalRepository, matchRepository,
        //        teamRepository, roundRepository, leagueRepository, playerRepository
        //).process();

        // Premijer liga
        new TransferMarktWorker(
            stadiumRepository, goalRepository, matchRepository,
            teamRepository, roundRepository, leagueRepository, playerRepository,
            "/premijer-liga/gesamtspielplan/wettbewerb/BOS1/saison_id/2018")
                .process();

        // Premier liga
        //new TransferMarktWorker(
        //    stadiumRepository, goalRepository, matchRepository,
        //    teamRepository, roundRepository, leagueRepository, playerRepository,
        //    "/premier-league/gesamtspielplan/wettbewerb/GB1/saison_id/2018")
        //    .process();
    }
}
