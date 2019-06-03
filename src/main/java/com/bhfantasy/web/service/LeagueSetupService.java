package com.bhfantasy.web.service;

import com.bhfantasy.web.model.LeagueSetup;
import com.bhfantasy.web.model.league.RegularLeague;
import com.bhfantasy.web.repository.LeagueSetupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class LeagueSetupService {

    private final LeagueSetupRepository leagueSetupRepository;

    private static Logger logger = LoggerFactory.getLogger(LeagueSetupService.class);

    public LeagueSetupService(LeagueSetupRepository leagueSetupRepository) {
        this.leagueSetupRepository = leagueSetupRepository;
    }

    @Transactional
    public void updateLeagueSetup(LeagueSetup setup, RegularLeague league) {
        Optional<LeagueSetup> optionalLeagueSetup = leagueSetupRepository.findById(setup.getId());

        if(optionalLeagueSetup.isPresent()) {
            setup = optionalLeagueSetup.get();

            if(setup.getLeague() == null) {
                setup.setLeague(league);

                leagueSetupRepository.save(setup);
            }
        }
    }
}
