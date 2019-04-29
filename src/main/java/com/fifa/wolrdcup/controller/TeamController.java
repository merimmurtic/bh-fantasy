package com.fifa.wolrdcup.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fifa.wolrdcup.exception.InvalidLeagueIdException;
import com.fifa.wolrdcup.exception.InvalidPlayerIdException;
import com.fifa.wolrdcup.exception.InvalidTeamIdException;
import com.fifa.wolrdcup.model.DefaultView;
import com.fifa.wolrdcup.model.League;
import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.repository.PlayerRepository;
import com.fifa.wolrdcup.repository.TeamRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    public TeamController(TeamRepository teamRepository, PlayerRepository playerRepository) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }

    @GetMapping
    @JsonView(DefaultView.class)
    public Iterable<Team> getTeams() throws Exception{
        return teamRepository.findAll();
    }

    @GetMapping("/{teamId}")
    @JsonView(Team.PlayersView.class)
    public ResponseEntity<Team> getTeam(@PathVariable("teamId") Long teamId) throws Exception {
        return ResponseEntity.of(teamRepository.findById(teamId));
    }

    @GetMapping("/search/{query}")
    public List<Team> searchTeams(@PathVariable("query") String query) throws Exception {
        return teamRepository.findByNameContaining(query);
    }

    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody Team team, UriComponentsBuilder builder) {
        // Make sure id is null to avoid update of existing league
        team.setId(null);

        try {
            team = teamRepository.save(team);

            //TODO: This is the right way to handle creation of model, try to do same for player and team creation
            return ResponseEntity.created(
                    builder.path("/teams/{id}").buildAndExpand(team.getId()).toUri()
            ).body(team);
        } catch (DataIntegrityViolationException e) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team with provided name already exist!");

        } catch (ConstraintViolationException e) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getConstraintViolations().toString());
        }
    }

/*
    @PostMapping("/{teamId}/add-player")
    public void referenceTeam(
            @PathVariable("teamId") Long teamId,
            @RequestBody Player player) {
        // TODO: Do implementation here, inside team object expect id only!
        // Load league for provided leagueId, update with it provided team and save it
        // Make sure to handle validation it provided leagueId doesn't exist, or provided team id doesn't exist
        if(player.getId() != null){
            Optional<Player> existingPlayerOptional = playerRepository.findById(player.getId());

            if(!existingPlayerOptional.isPresent()) {
                throw new InvalidPlayerIdException();
            }

            Optional<Team> existingTeamOptional = teamRepository.findById(teamId);

            if(!existingTeamOptional.isPresent()) {
                throw new InvalidTeamIdException();
            }

            Team team = existingTeamOptional.get();

            player = existingPlayerOptional.get();

            if(!player.getTeams().contains(team)) {
                player.getTeams().add(team);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Player is already referenced with provided league!");
            }

            playerRepository.save(player);
        } else {
            throw new InvalidPlayerIdException();
        }
    }

 */


}

