package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.model.League;
import com.fifa.wolrdcup.model.Round;
import com.fifa.wolrdcup.repository.LeagueRepository;
import com.fifa.wolrdcup.repository.RoundRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rounds")
public class RoundController {

    private final RoundRepository roundRepository;
    private final LeagueRepository leagueRepository;

    public RoundController(RoundRepository roundRepository, LeagueRepository leagueRepository){
        this.roundRepository = roundRepository;
        this.leagueRepository = leagueRepository;
    }
    @GetMapping("/{leagueId}")
    public ResponseEntity<League> getLeagueRounds(@PathVariable("leagueId") Long leagueId) throws Exception {
        return ResponseEntity.of(leagueRepository.findById(leagueId));
    }

}
