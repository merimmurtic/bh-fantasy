package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.repository.TeamRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/standings")
public class StandingController {

    private final TeamRepository teamRepository;

    public StandingController(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }


}
