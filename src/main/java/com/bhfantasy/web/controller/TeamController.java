package com.bhfantasy.web.controller;

import com.bhfantasy.web.exception.InvalidPlayerIdException;
import com.bhfantasy.web.model.Team;
import com.bhfantasy.web.model.league.FantasyLeague;
import com.bhfantasy.web.model.players.*;
import com.fasterxml.jackson.annotation.JsonView;
import com.bhfantasy.web.exception.InvalidTeamIdException;
import com.bhfantasy.web.model.User;
import com.bhfantasy.web.model.views.DefaultView;
import com.bhfantasy.web.repository.PlayerRepository;
import com.bhfantasy.web.repository.TeamRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.ConstraintViolationException;
import java.util.*;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    public TeamController(TeamRepository teamRepository, PlayerRepository playerRepository) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }

    @GetMapping
    @JsonView(DefaultView.class)
    public Iterable<Team> getTeams() throws Exception {
        return teamRepository.findAll();
    }

    @GetMapping("/{teamId}")
    @JsonView(value = {Team.DetailedView.class})
    public ResponseEntity<Team> getTeam(@PathVariable("teamId") Long teamId) throws Exception {
        return ResponseEntity.of(teamRepository.findById(teamId));
    }

    @GetMapping("/search/{query}")
    @JsonView(DefaultView.class)
    public List<Team> searchTeams(@PathVariable("query") String query) throws Exception {
        return teamRepository.findByNameContaining(query);
    }

    @PostMapping
    @JsonView(Team.DetailedView.class)
    @Secured("ROLE_USER")
    public ResponseEntity<Team> createTeam(
            @RequestBody Team team, UriComponentsBuilder builder, @AuthenticationPrincipal User user) {
        // Make sure id is null to avoid update of existing league
        team.setId(null);

        if (team.getCode() == null) {
            team.setCode(team.getName());
        }

        team.setUser(user);

        try {
            team = teamRepository.save(team);

            return ResponseEntity.created(
                    builder.path("/teams/{id}").buildAndExpand(team.getId()).toUri()
            ).body(team);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team with provided code already exist!");
        } catch (ConstraintViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getConstraintViolations().toString());
        }
    }

    @PostMapping("/{teamId}/add-player")
    public void referenceTeam(
            @PathVariable("teamId") Long teamId,
            @RequestBody Player player) {

        if (player.getId() != null) {
            Optional<Player> existingPlayerOptional = playerRepository.findById(player.getId());

            if (!existingPlayerOptional.isPresent()) {
                throw new InvalidPlayerIdException();
            }

            Optional<Team> existingTeamOptional = teamRepository.findById(teamId);

            if (!existingTeamOptional.isPresent()) {
                throw new InvalidTeamIdException();
            }

            Team team = existingTeamOptional.get();

            player = existingPlayerOptional.get();

            if (team.getLeagues().size() > 0 && team.getLeagues().iterator().next() instanceof FantasyLeague) {
                List<Player> players = team.getPlayersOfType(player.getClass());

                if (player instanceof Goalkeaper) {
                    if (players.size() > 1) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum Goalkeepers!");

                    }
                } else if (player instanceof Defender) {
                    if (players.size() > 4) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum Defenders!");
                    }
                } else if (player instanceof Middle) {
                    if (players.size() > 4) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum Middles!");
                    }
                } else if (player instanceof Striker) {
                    if (players.size() > 2) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum Strikers!");
                    }
                }
            }

            if (!player.getTeams().contains(team)) {
                player.getTeams().add(team);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Player is already referenced with provided team!");
            }

            playerRepository.save(player);
        } else {
            throw new InvalidPlayerIdException();
        }
    }
}