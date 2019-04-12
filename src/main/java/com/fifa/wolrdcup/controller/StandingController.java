package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.repository.TeamRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@RestController
@RequestMapping("/standings")
public class StandingController {

    private final TeamRepository teamRepository;

    public StandingController(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @GetMapping
    public Iterable<HashMap<String, Object>> getStandings() {
        List<HashMap<String, Object>> result = new ArrayList<>();

        Iterable<Team> teams = teamRepository.findAll();

        for(Team team : teams){
            HashMap<String, Object> teamMap = new HashMap<>();
            teamMap.put("teamId", team.getId());
            teamMap.put("teamName", team.getName());

            result.add(teamMap);
        }

        return result;
    }
}
