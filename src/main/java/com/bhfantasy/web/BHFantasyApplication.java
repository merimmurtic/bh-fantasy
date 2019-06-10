package com.bhfantasy.web;

import com.bhfantasy.web.model.LeagueSetup;
import com.bhfantasy.web.model.league.RegularLeague;
import com.bhfantasy.web.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.*;

@SpringBootApplication
@EnableScheduling
public class BHFantasyApplication {

    private static Logger logger = LoggerFactory.getLogger(BHFantasyApplication.class);

    private final LeagueSetupRepository leagueSetupRepository;

    public BHFantasyApplication(
            LeagueSetupRepository leagueSetupRepository) {
        this.leagueSetupRepository = leagueSetupRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(BHFantasyApplication.class, args);
    }

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Collections.singletonList("*"));
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization", "x-xsrf-token"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @PostConstruct
    @Transactional
    public void seedLeagueSetups() {
        String seasonId = "2018";

        if(leagueSetupRepository.count() == 0) {
            saveLeagueSetup("Premijer Liga","/premijer-liga/gesamtspielplan/wettbewerb/BOS1", seasonId);

            saveLeagueSetup(
                    "Euro Qualification",
                    "/em-qualifikation/gesamtspielplan/pokalwettbewerb/EMQ",
                    seasonId, RegularLeague.Level.NATION);

            saveLeagueSetup("Champions league","/uefa-champions-league/gesamtspielplan/pokalwettbewerb/CL", seasonId);

            LeagueSetup top5LeagueSetup = new LeagueSetup();
            top5LeagueSetup.setName("TOP 5 League Setup");

            top5LeagueSetup.getLeagueSetups().add(
              saveLeagueSetup("Premier League","/premier-league/gesamtspielplan/wettbewerb/GB1", seasonId));
            top5LeagueSetup.getLeagueSetups().add(
                    saveLeagueSetup("Bundesliga","/1-bundesliga/gesamtspielplan/wettbewerb/L1", seasonId));
            top5LeagueSetup.getLeagueSetups().add(
                    saveLeagueSetup("Serie A","/serie-a/gesamtspielplan/wettbewerb/IT1", seasonId));
            top5LeagueSetup.getLeagueSetups().add(
                    saveLeagueSetup("Primera Division","/primera-division/gesamtspielplan/wettbewerb/ES1", seasonId));
            top5LeagueSetup.getLeagueSetups().add(
                    saveLeagueSetup("Ligue 1","/ligue-1/gesamtspielplan/wettbewerb/FR1", seasonId));

            leagueSetupRepository.save(top5LeagueSetup);

            logger.info("League setups are saved successfully");
        };
    }

    private LeagueSetup saveLeagueSetup(String name, String transferMarktUrl, String seasonId) {
        return saveLeagueSetup(name, transferMarktUrl, seasonId, RegularLeague.Level.CLUB);
    }

    private LeagueSetup saveLeagueSetup(String name, String transferMarktUrl, String seasonId, RegularLeague.Level level) {
        LeagueSetup setup = new LeagueSetup();
        setup.setName(name);
        setup.setLevel(level);
        setup.setTransfermarktUrl(
                transferMarktUrl.concat("/saison_id/").concat(seasonId));

        return leagueSetupRepository.save(setup);
    }
}
