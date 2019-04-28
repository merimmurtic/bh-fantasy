package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.exception.InvalidLeagueIdException;
import com.fifa.wolrdcup.exception.InvalidTeamIdException;
import com.fifa.wolrdcup.model.League;
import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.repository.LeagueRepository;
import com.fifa.wolrdcup.repository.TeamRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.ConstraintViolationException;
import java.util.Optional;

@RestController
@RequestMapping("/leagues")
public class LeagueController {

    private final LeagueRepository leagueRepository;

    private final TeamRepository teamRepository;

    public LeagueController(TeamRepository teamRepository,
            LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
        this.teamRepository = teamRepository;
    }

    @GetMapping
    public Iterable<League> getLeagues() throws Exception{
        return leagueRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<League> createLeague(@RequestBody League league, UriComponentsBuilder builder) {
        // Make sure id is null to avoid update of existing league
        league.setId(null);

        try {
            league = leagueRepository.save(league);

            //TODO: This is the right way to handle creation of model, try to do same for player and team creation
            return ResponseEntity.created(
                    builder.path("/leagues/{id}").buildAndExpand(league.getId()).toUri()
            ).body(league);
        } catch (DataIntegrityViolationException e) {
            // DataIntegrityViolationException is thrown in case unique constraint fails!
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "League with provided name already exist!");
        } catch (ConstraintViolationException e) {
            // In case validation of model fail ConstraintViolationException is thrown (for example, name is null)
            // in this case throw ResponseStatusException with details about contstraint violations
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getConstraintViolations().toString());
        }
    }

    @PostMapping("/{leagueId}/add-team")
    public void referenceTeam(
            @PathVariable("leagueId") Long leagueId,
            @RequestBody Team team) {
        // TODO: Do implementation here, inside team object expect id only!
        // Load league for provided leagueId, update with it provided team and save it
        // Make sure to handle validation it provided leagueId doesn't exist, or provided team id doesn't exist
        if(team.getId() != null){
            Optional<Team> existingTeamOptional = teamRepository.findById(team.getId());

            if(!existingTeamOptional.isPresent()) {
                throw new InvalidTeamIdException();
            }

            Optional<League> existingLeagueOptional = leagueRepository.findById(leagueId);

            if(!existingLeagueOptional.isPresent()) {
                throw new InvalidLeagueIdException();
            }

            League league = existingLeagueOptional.get();

            team = existingTeamOptional.get();

            if(!team.getLeagues().contains(league)) {
                team.getLeagues().add(league);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Team is already referenced with provided league!");
            }

            teamRepository.save(team);
        } else {
            throw new InvalidTeamIdException();
        }
    }

    @GetMapping("/{leagueId}")
    public ResponseEntity<League> getLeague(@PathVariable("leagueId") Long leagueId) throws Exception {
        return ResponseEntity.of(leagueRepository.findById(leagueId));
    }
}