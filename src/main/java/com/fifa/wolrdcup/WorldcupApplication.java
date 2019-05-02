package com.fifa.wolrdcup;

import com.fifa.wolrdcup.repository.*;
import com.fifa.wolrdcup.service.FantasyService;
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

    private final LineupRepository lineupRepository;

    private final SubstitutionRepository substitutionRepository;

    private final CardRepository cardRepository;

    private final FantasyService fantasyService;

    public WorldcupApplication(
            FantasyService fantasyService,
            StadiumRepository stadiumRepository,
            GoalRepository goalRepository,
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            RoundRepository roundRepository,
            LeagueRepository leagueRepository,
            PlayerRepository playerRepository,
            LineupRepository lineupRepository,
            SubstitutionRepository substitutionRepository,
            CardRepository cardRepository) {
        this.leagueRepository = leagueRepository;
        this.roundRepository = roundRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.goalRepository = goalRepository;
        this.stadiumRepository = stadiumRepository;
        this.lineupRepository = lineupRepository;
        this.fantasyService = fantasyService;
        this.substitutionRepository = substitutionRepository;
        this.cardRepository = cardRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(WorldcupApplication.class, args);
    }

    @Bean
    InitializingBean seedDatabase() {
        return () -> {
            startWorkers();
        };
    }

    private void startWorkers() throws Exception {
        WorldCupWorker worldCupWorker = new WorldCupWorker(
                stadiumRepository, goalRepository, matchRepository,
                teamRepository, roundRepository, leagueRepository, playerRepository
        );

        TransferMarktWorker premijerLigaWorker = new TransferMarktWorker(
            stadiumRepository, goalRepository, matchRepository,
            teamRepository, roundRepository, leagueRepository,
                playerRepository, lineupRepository, substitutionRepository, cardRepository,
            "/premijer-liga/gesamtspielplan/wettbewerb/BOS1/saison_id/2018");

        TransferMarktWorker premierLeagueWorker = new TransferMarktWorker(
            stadiumRepository, goalRepository, matchRepository,
            teamRepository, roundRepository, leagueRepository,
                playerRepository, lineupRepository, substitutionRepository, cardRepository,
            "/premier-league/gesamtspielplan/wettbewerb/GB1/saison_id/2018");

        Long leagueId = premijerLigaWorker.process();

        if(leagueId != null) {
            this.fantasyService.process(leagueId);
        }
    }
}
