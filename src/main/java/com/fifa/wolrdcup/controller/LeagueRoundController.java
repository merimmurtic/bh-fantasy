package com.fifa.wolrdcup.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fifa.wolrdcup.model.Round;
import com.fifa.wolrdcup.model.views.DefaultView;
import com.fifa.wolrdcup.repository.RoundRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/leagues/{leagueId}/rounds")
public class LeagueRoundController {

    private final RoundRepository roundRepository;

    public LeagueRoundController(RoundRepository roundRepository){
        this.roundRepository = roundRepository;
    }

    @GetMapping
    @JsonView(DefaultView.class)
    public Iterable<Round> getLeagueRounds(@PathVariable("leagueId") Long leagueId) throws Exception{
        return roundRepository.findByLeagueId(leagueId);
    }
}
