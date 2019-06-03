package com.bhfantasy.web.controller;

import com.bhfantasy.web.exception.PlayerNotFoundException;
import com.bhfantasy.web.model.Goal;
import com.bhfantasy.web.model.players.Player;
import com.fasterxml.jackson.annotation.JsonView;
import com.bhfantasy.web.exception.InvalidPlayerPositionException;
import com.bhfantasy.web.model.views.DefaultView;
import com.bhfantasy.web.repository.GoalRepository;
import com.bhfantasy.web.repository.PlayerRepository;

import com.bhfantasy.web.repository.TeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final PlayerRepository playerRepository;

    private final GoalRepository goalRepository;

    private final TeamRepository teamRepository;

    public PlayerController(
            GoalRepository goalRepository,
            PlayerRepository playerRepository, TeamRepository teamRepository) {
        this.playerRepository = playerRepository;
        this.goalRepository = goalRepository;
        this.teamRepository = teamRepository;
    }

    @GetMapping("/{playerId}")
    @JsonView(Player.DetailedView.class)
    public ResponseEntity<Player> getPlayer(@PathVariable("playerId") Long playerId) {
        Optional<Player> playerOptional = playerRepository.findById(playerId);

        playerOptional.ifPresent(player -> player.setTeams(teamRepository.getTeams(playerId)));

        return ResponseEntity.of(playerOptional);
    }

    @GetMapping
    @JsonView(DefaultView.class)
    public Iterable<Player> getPlayers(
            @RequestParam(value = "leagueId", required = false) Long leagueId,
            @RequestParam(value = "teamId", required = false) Long teamId) {
        if (teamId != null && leagueId != null) {
            return playerRepository.findByTeamsAndTeams_Leagues_Id(teamId, leagueId);
        } else if (teamId != null) {
            return playerRepository.findByTeams(teamId);
        } else if (leagueId != null) {
            return playerRepository.findDistinctByTeams_Leagues_Id(leagueId);
        } else {
            return playerRepository.findAll();
        }
    }

    @PostMapping
    @JsonView(Player.DetailedView.class)
    public ResponseEntity<Player> createPlayer(@RequestBody Player player, UriComponentsBuilder builder) {
        // Make sure id is null to avoid update of existing league
        player.setId(null);

        try {
            player = playerRepository.save(player);

            return ResponseEntity.created(
                    builder.path("/players/{id}").buildAndExpand(player.getId()).toUri()
            ).body(player);
        } catch (ConstraintViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getConstraintViolations().toString());
        }
    }

    @PutMapping
    @JsonView(Player.DetailedView.class)
    public Player putPlayer(@RequestBody Player player) {
        if (player.getId() == null) {
            throw new InvalidPlayerPositionException();
        }

        Optional<Player> existingPlayerOptional = playerRepository.findById(player.getId());

        if (existingPlayerOptional.isPresent()) {
            Player existingPlayer = existingPlayerOptional.get();

            // In case player type is changed, player migration needs to be done because
            // it's not same type of player and new player needs to be created.
            if (!existingPlayer.getType().equals(player.getType())) {
                // Do player migration and return new version of player
                return migratePlayer(existingPlayer, player);
            } else {
                // Update provided fields on existing player including team if it's provided
                if (player.getNumberoOnDress() != null) {
                    existingPlayer.setNumberoOnDress(player.getNumberoOnDress());
                }

                if (player.getFirstName() != null) {
                    existingPlayer.setFirstName(player.getFirstName());
                }

                if (player.getLastName() != null) {
                    existingPlayer.setLastName(player.getLastName());
                }

                if (player.getPosition() != null) {
                    existingPlayer.setPosition(player.getPosition());
                }

                // Update existing player
                return playerRepository.save(existingPlayer);
            }
        } else {
            // Throw exception if player is not found
            throw new PlayerNotFoundException();
        }
    }

    /**
     * Migrate player to new player.
     * Method move all existing references to player to new player.
     * In the end new player is returned.
     *
     * @param oldPlayer
     * @param newPlayer
     * @return
     */
    private Player migratePlayer(Player oldPlayer, Player newPlayer) {
        // Reset id to prevent update of existing player, we want to force player creation
        newPlayer.setId(null);
        newPlayer.getTeams().addAll(oldPlayer.getTeams());
        newPlayer.setTransferMarktId(oldPlayer.getTransferMarktId());

        if (newPlayer.getNumberoOnDress() == null) {
            newPlayer.setNumberoOnDress(oldPlayer.getNumberoOnDress());
        }

        if (newPlayer.getFirstName() == null) {
            newPlayer.setFirstName(oldPlayer.getFirstName());
        }

        if (newPlayer.getLastName() == null) {
            newPlayer.setLastName(oldPlayer.getLastName());
        }

        if (newPlayer.getPosition() == null) {
            newPlayer.setPosition(oldPlayer.getPosition());
        }

        // Save new player
        newPlayer = playerRepository.save(newPlayer);

        // Move all goals to new version of player
        List<Goal> goals = goalRepository.findByPlayer(oldPlayer);

        for (Goal goal : goals) {
            goal.setPlayer(newPlayer);
            goalRepository.save(goal);
        }

        // Remove old player
        playerRepository.delete(oldPlayer);

        return newPlayer;
    }
}
