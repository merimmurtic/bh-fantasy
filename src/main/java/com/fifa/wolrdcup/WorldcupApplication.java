package com.fifa.wolrdcup;

import com.fifa.wolrdcup.repository.*;
import com.fifa.wolrdcup.service.FantasyService;
import com.fifa.wolrdcup.service.PlayerService;
import com.fifa.wolrdcup.workers.ProcessWorker;
import com.fifa.wolrdcup.workers.TransferMarktWorker;
import com.fifa.wolrdcup.workers.WorldCupWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class WorldcupApplication {

    private static Logger logger = LoggerFactory.getLogger(WorldcupApplication.class);

    private final StadiumRepository stadiumRepository;

    private final LeagueRepository leagueRepository;

    private final RoundRepository roundRepository;

    private final TeamRepository teamRepository;

    private final MatchRepository matchRepository;

    private final PlayerService playerService;

    private final GoalRepository goalRepository;

    private final LineupRepository lineupRepository;

    private final SubstitutionRepository substitutionRepository;

    private final CardRepository cardRepository;

    private final FantasyService fantasyService;

    private final MissedPenaltyRepository missedPenaltyRepository;

    private static boolean WORKERS_RUNNING = false;

    private final String PREMIJER_LIGA_URL = "/premijer-liga/gesamtspielplan/wettbewerb/BOS1/saison_id/2018";

    private final String[] TRANSFERMARKT_URLS = new String[] {
        PREMIJER_LIGA_URL,
        "/serie-a/gesamtspielplan/wettbewerb/IT1/saison_id/2018",
        "/1-bundesliga/gesamtspielplan/wettbewerb/L1/saison_id/2018",
        "/premier-league/gesamtspielplan/wettbewerb/GB1/saison_id/2018",
        "/primera-division/gesamtspielplan/wettbewerb/ES1/saison_id/2018"
    };

    public WorldcupApplication(
            FantasyService fantasyService,
            StadiumRepository stadiumRepository,
            GoalRepository goalRepository,
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            RoundRepository roundRepository,
            LeagueRepository leagueRepository,
            PlayerService playerService,
            LineupRepository lineupRepository,
            SubstitutionRepository substitutionRepository,
            CardRepository cardRepository,
            MissedPenaltyRepository missedPenaltyRepository) {
        this.leagueRepository = leagueRepository;
        this.roundRepository = roundRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.playerService = playerService;
        this.goalRepository = goalRepository;
        this.stadiumRepository = stadiumRepository;
        this.lineupRepository = lineupRepository;
        this.fantasyService = fantasyService;
        this.substitutionRepository = substitutionRepository;
        this.cardRepository = cardRepository;
        this.missedPenaltyRepository = missedPenaltyRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(WorldcupApplication.class, args);
    }

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Collections.singletonList("*"));
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    // Method will be executed each hour to refresh leagues
    public void startWorkers() throws Exception {
        if(WORKERS_RUNNING) {
            return;
        }

        try {
            WORKERS_RUNNING = true;

            List<ProcessWorker> workers = new ArrayList<>();

            workers.add(new WorldCupWorker(
                    stadiumRepository, goalRepository, matchRepository,
                    teamRepository, roundRepository, leagueRepository, playerService
            ));


            for (String url : TRANSFERMARKT_URLS) {
                workers.add(new TransferMarktWorker(
                        stadiumRepository, goalRepository, matchRepository,
                        teamRepository, roundRepository, leagueRepository,
                        playerService, lineupRepository, substitutionRepository, cardRepository, missedPenaltyRepository,
                        url));
            }

            for (ProcessWorker worker : workers) {
                Long leagueId = worker.process();

                if (leagueId != null) {
                    this.fantasyService.process(leagueId);
                }

                if(worker instanceof TransferMarktWorker) {
                    if(((TransferMarktWorker) worker).getTransfermarktUrl().equals(PREMIJER_LIGA_URL)) {
                        fantasyService.seedFantasyPlayerLeague(leagueId);
                    }
                }

                Thread.sleep(10000);
            }
        } finally {
            WORKERS_RUNNING = false;
        }
    }
}
