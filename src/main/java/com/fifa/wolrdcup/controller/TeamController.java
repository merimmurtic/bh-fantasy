package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.repository.TeamRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;
import java.util.List;

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
    public Team createTeam(@RequestBody Team team) throws Exception {
        // Make sure id is null to avoid update of existing team
        team.setId(null);

        try {
            return teamRepository.save(team);
        } catch (DataIntegrityViolationException e) {
            // DataIntegrityViolationException is thrown in case unique constraint fails!
            // You can throw exception with corresponding status (400 = BAD_REQUEST) and details in this way also,
            // instead of creating new exception class (e.g. TeamWithProvidedCodeAlreadyExistException)
            // Use this for cases when you need to throw specific exception which will be thrown only in one place
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team with provided code already exist!");
        } catch (ConstraintViolationException e) {
            // In case validation of model fail (for example, name is null) throw exception with details
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getConstraintViolations().toString());
        }
    }

}

