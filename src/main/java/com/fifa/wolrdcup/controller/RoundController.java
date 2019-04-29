package com.fifa.wolrdcup.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fifa.wolrdcup.model.DefaultView;
import com.fifa.wolrdcup.model.Round;
import com.fifa.wolrdcup.repository.RoundRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rounds")
public class RoundController {

    private final RoundRepository roundRepository;

    public RoundController(RoundRepository roundRepository){
        this.roundRepository = roundRepository;
    }

    @GetMapping("/{leagueId}")
    @JsonView(DefaultView.class)
    public Iterable<Round> getLeagueRounds(@PathVariable("leagueId") Long leagueId) throws Exception{

        return roundRepository.findByLeagueId(leagueId);
    }

}
