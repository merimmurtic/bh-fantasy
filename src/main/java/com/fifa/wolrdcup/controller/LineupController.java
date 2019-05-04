package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.model.Lineup;
import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.repository.LeagueRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/leagues/{leagueId}/rounds/{roundId}/teams/{teamId}")
public class LineupController {

    private final LeagueRepository leagueRepository;

    public LineupController(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @PostMapping
    public  ResponseEntity<Lineup> FantasyLineup() {

        return null;
    }

}

