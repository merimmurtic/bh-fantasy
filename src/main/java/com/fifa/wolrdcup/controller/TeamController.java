package com.fifa.wolrdcup.controller;


import com.fifa.wolrdcup.exception.InvalidTeamIdException;
import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.repository.TeamRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamRepository teamRepository;

    public TeamController(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }
    @GetMapping
    public Iterable<Team> getTeams() throws Exception{
        return teamRepository.findAll();
    }
    @GetMapping("/{teamId}")
    public ResponseEntity<Team> getTeam(@PathVariable("teamId") Long teamId) throws Exception {
        return ResponseEntity.of(teamRepository.findById(teamId));
    }

    @GetMapping("/search/{query}")
    public List<Team> searchTeams(@PathVariable("query") String query) throws Exception {
        return teamRepository.findByNameContaining(query);
    }
    @PostMapping
    public void createTeam(@RequestBody Team team) throws Exception {
        if(team.getId() != null){
            Optional<Team> existingTeamOptional = teamRepository.findById(team.getId());
            if(existingTeamOptional.isPresent()){
                throw new InvalidTeamIdException();
            } else{
                teamRepository.save(team);
            }

        }

    }

}

