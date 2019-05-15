package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.model.custom.TopPlayerValue;
import com.fifa.wolrdcup.repository.LeagueRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("leagues/{leagueId}/top-players")
public class TopPlayerController {

    private final LeagueRepository leagueRepository;

    public TopPlayerController(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @GetMapping
    public List<TopPlayerValue> getTopPlayers(
            @PathVariable("leagueId") Long leagueId) {
        return leagueRepository.getTopPlayers(leagueId);
    }
}
