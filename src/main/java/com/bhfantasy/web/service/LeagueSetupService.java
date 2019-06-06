package com.bhfantasy.web.service;

import com.bhfantasy.web.model.LeagueSetup;
import com.bhfantasy.web.model.league.FantasyLeague;
import com.bhfantasy.web.model.league.RegularLeague;
import com.bhfantasy.web.repository.LeagueSetupRepository;
import com.bhfantasy.web.workers.TransferMarktWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LeagueSetupService {

    private final LeagueSetupRepository leagueSetupRepository;

    private final TransferMarktWorker transferMarktWorker;

    private static Logger logger = LoggerFactory.getLogger(LeagueSetupService.class);

    private static boolean WORKERS_RUNNING = false;

    public LeagueSetupService(LeagueSetupRepository leagueSetupRepository, TransferMarktWorker transferMarktWorker) {
        this.leagueSetupRepository = leagueSetupRepository;
        this.transferMarktWorker = transferMarktWorker;
    }

    @Transactional
    public LeagueSetup updateLeagueSetup(LeagueSetup setup, RegularLeague league, FantasyLeague fantasyLeague) {
        Optional<LeagueSetup> optionalLeagueSetup = leagueSetupRepository.findById(setup.getId());

        if(optionalLeagueSetup.isPresent()) {
            setup = optionalLeagueSetup.get();

            if(setup.getLeague() == null) {
                setup.setLeague(league);
            }

            if(setup.getFantasyLeague() == null) {
                setup.setFantasyLeague(fantasyLeague);
            }

            return leagueSetupRepository.save(setup);
        }

        return null;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void startWorkers() throws Exception {
        if (WORKERS_RUNNING) {
            return;
        }

        try {
            WORKERS_RUNNING = true;

            Iterable<LeagueSetup> leagueSetups = leagueSetupRepository.findLeaguesBetween(
                    LocalDateTime.now(), LocalDateTime.now().plusDays(1));

            for(LeagueSetup setup : leagueSetups) {
                if(setup.getTransfermarktUrl() != null) {
                    transferMarktWorker.process(setup.getTransfermarktUrl());
                }
            }
        } finally {
            WORKERS_RUNNING = false;
        }
    }
}
