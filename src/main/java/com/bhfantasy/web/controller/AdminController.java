package com.bhfantasy.web.controller;

import com.bhfantasy.web.model.LeagueSetup;
import com.bhfantasy.web.model.league.FantasyLeague;
import com.bhfantasy.web.model.league.RegularLeague;
import com.bhfantasy.web.repository.*;
import com.bhfantasy.web.service.FantasyService;
import com.bhfantasy.web.service.LeagueSetupService;
import com.bhfantasy.web.service.MultiLeagueService;
import com.bhfantasy.web.workers.TransferMarktWorker;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@Secured("ROLE_ADMIN")
@RequestMapping("/api/admin")
public class AdminController {

    private final LeagueSetupRepository leagueSetupRepository;

    private final LeagueSetupService leagueSetupService;

    private final TransferMarktWorker transferMarktWorker;

    private final MultiLeagueService multiLeagueService;

    private final FantasyService fantasyService;

    public AdminController(LeagueSetupRepository leagueSetupRepository,
                           LeagueSetupService leagueSetupService, TransferMarktWorker transferMarktWorker,
                           MultiLeagueService multiLeagueService, FantasyService fantasyService) {
        this.leagueSetupRepository = leagueSetupRepository;
        this.leagueSetupService = leagueSetupService;
        this.transferMarktWorker = transferMarktWorker;
        this.multiLeagueService = multiLeagueService;
        this.fantasyService = fantasyService;
    }

    @GetMapping("/setups")
    @JsonView(LeagueSetup.DetailedView.class)
    public Iterable<LeagueSetup> getLeagueSetups() throws Exception{
        return leagueSetupRepository.findAll();
    }

    @PostMapping("/setups")
    @JsonView(LeagueSetup.DetailedView.class)
    public LeagueSetup addLeagueSetup(
            @RequestBody LeagueSetup setup) {
        setup.setId(null);

        return leagueSetupRepository.save(setup);
    }

    @PostMapping("/setups/{setupId}/createFantasyLeague")
    @JsonView(LeagueSetup.DetailedView.class)
    public LeagueSetup createFantasyLeague(@PathVariable("setupId") Long setupId) {
        Optional<LeagueSetup> optionalLeagueSetup = leagueSetupRepository.findById(setupId);

        if (optionalLeagueSetup.isPresent()) {
            LeagueSetup leagueSetup = optionalLeagueSetup.get();

            if(leagueSetup.getLeague() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "League setup is not processed!");
            }

            FantasyLeague fantasyLeague = fantasyService.createFantasyPlayerLeague(leagueSetup.getLeague().getId());

            return leagueSetupService.updateLeagueSetup(leagueSetup, null, fantasyLeague);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "League setup doesn't exist!");
        }
    }

    @PostMapping("/setups/{setupId}/process")
    @Transactional
    @JsonView(LeagueSetup.DetailedView.class)
    public LeagueSetup processLeagueSetup(@PathVariable("setupId") Long setupId) {
        Optional<LeagueSetup> optionalLeagueSetup = leagueSetupRepository.findById(setupId);

        if (optionalLeagueSetup.isPresent()) {
            LeagueSetup leagueSetup = optionalLeagueSetup.get();

            if (leagueSetup.getTransfermarktUrl() != null) {
                Executors.newSingleThreadExecutor().execute(
                        () -> {
                            RegularLeague league = transferMarktWorker.process(leagueSetup.getTransfermarktUrl());

                            leagueSetupService.updateLeagueSetup(leagueSetup, league, null);
                        }
                );
            } else if(leagueSetup.getLeagueSetups().size() > 0) {
                RegularLeague regularLeague = multiLeagueService.seedTop5League(
                        leagueSetup.getLeagueSetups().stream().map(setup -> {
                            if(setup.getLeague() == null) {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST, "One of league setups is not processed!");
                            }

                            return setup.getLeague().getId();
                        }).collect(Collectors.toList()),
                        leagueSetup.getName());

                leagueSetup.setLeague(regularLeague);

                leagueSetupRepository.save(leagueSetup);
            }

            return leagueSetup;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "League setup doesn't exist!");
        }
    }
}
