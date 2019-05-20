package com.fifa.wolrdcup;

import com.fifa.wolrdcup.model.league.RegularLeague;
import com.fifa.wolrdcup.repository.*;
import com.fifa.wolrdcup.service.FantasyService;
import com.fifa.wolrdcup.service.MultiLeagueService;
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

import javax.transaction.Transactional;
import java.util.*;

@SpringBootApplication
@EnableScheduling
public class WorldcupApplication {

    private static Logger logger = LoggerFactory.getLogger(WorldcupApplication.class);

    private final StadiumRepository stadiumRepository;

    private final RegularLeagueRepository regularLeagueRepository;

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

    private final MultiLeagueService multiLeagueService;

    private static boolean WORKERS_RUNNING = false;

    private static final String PREMIJER_LIGA_URL = "/premijer-liga/gesamtspielplan/wettbewerb/BOS1/saison_id/";

    private static final List<String> TOP_5_URLS = Arrays.asList(
        "/serie-a/gesamtspielplan/wettbewerb/IT1/saison_id/",
        "/1-bundesliga/gesamtspielplan/wettbewerb/L1/saison_id/",
        "/premier-league/gesamtspielplan/wettbewerb/GB1/saison_id/",
        "/primera-division/gesamtspielplan/wettbewerb/ES1/saison_id/",
        "/ligue-1/gesamtspielplan/wettbewerb/FR1/saison_id/");

    private final static List<String> TRANSFERMARKT_URLS = new ArrayList<>();

    static {
        TRANSFERMARKT_URLS.add(PREMIJER_LIGA_URL);
        TRANSFERMARKT_URLS.addAll(TOP_5_URLS);
    }

    public WorldcupApplication(
            FantasyService fantasyService,
            StadiumRepository stadiumRepository,
            GoalRepository goalRepository,
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            RoundRepository roundRepository,
            RegularLeagueRepository regularLeagueRepository,
            PlayerService playerService,
            LineupRepository lineupRepository,
            SubstitutionRepository substitutionRepository,
            CardRepository cardRepository,
            MissedPenaltyRepository missedPenaltyRepository, MultiLeagueService multiLeagueService) {
        this.regularLeagueRepository = regularLeagueRepository;
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
        this.multiLeagueService = multiLeagueService;
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
                    teamRepository, roundRepository, regularLeagueRepository, playerService, "2014"
            ));


            for (String url : TRANSFERMARKT_URLS) {
                workers.add(new TransferMarktWorker(
                        stadiumRepository, goalRepository, matchRepository,
                        teamRepository, roundRepository, regularLeagueRepository,
                        playerService, lineupRepository, substitutionRepository, cardRepository, missedPenaltyRepository,
                        url, "2018"));
            }

            List<Long> top5LeagueIds = new ArrayList<>();

            for (ProcessWorker worker : workers) {
                Long leagueId = worker.process();

                if (leagueId != null) {
                    this.fantasyService.process(leagueId);
                }

                if(worker instanceof TransferMarktWorker) {
                    if(((TransferMarktWorker) worker).getTransfermarktUrl().equals(PREMIJER_LIGA_URL)) {
                        fantasyService.seedFantasyPlayerLeague(leagueId);
                    }

                    if(TOP_5_URLS.contains(((TransferMarktWorker) worker).getTransfermarktUrl())) {
                        top5LeagueIds.add(leagueId);
                    }
                }

                Thread.sleep(10000);
            }

            seedTop5League(top5LeagueIds);
        } finally {
            WORKERS_RUNNING = false;
        }
    }

    @Transactional
    public void seedTop5League(List<Long> leagueIds) {
        List<RegularLeague> regularLeagues = new ArrayList<>();

        regularLeagueRepository.findAllById(leagueIds).forEach(regularLeagues::add);

        if(regularLeagues.size() > 0) {
            RegularLeague firstLeague = regularLeagues.get(0);

            Optional<RegularLeague> top5LeagueOptional = regularLeagueRepository.findByNameAndSeason(
                    "TOP 5 League", firstLeague.getSeason());

            if (!top5LeagueOptional.isPresent()) {
                RegularLeague top5League = new RegularLeague();
                top5League.setName("TOP 5 League");
                top5League.setSeason(firstLeague.getSeason());
                top5League.getGroups().addAll(regularLeagues);

                regularLeagueRepository.save(top5League);

                regularLeagues.forEach(league -> {
                    league.getTeams().forEach(team -> {
                        team.getLeagues().add(top5League);

                        teamRepository.save(team);
                    });
                });

                multiLeagueService.calculateRounds(top5League);
            } else {
                logger.info("Top 5 League is already created for season {}", firstLeague.getSeason());
            }
        }
    }
}
