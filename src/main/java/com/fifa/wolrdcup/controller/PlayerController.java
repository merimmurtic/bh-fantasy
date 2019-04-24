package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.exception.PlayerNotFoundException;
import com.fifa.wolrdcup.exception.TeamNotFoundException;
import com.fifa.wolrdcup.model.Goal;
import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.repository.GoalRepository;
import com.fifa.wolrdcup.repository.PlayerRepository;
import com.fifa.wolrdcup.repository.TeamRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/players")
public class PlayerController {
    private final PlayerRepository playerRepository;

    private final TeamRepository teamRepository;

    private final GoalRepository goalRepository;

    public PlayerController(
            GoalRepository goalRepository,
            PlayerRepository playerRepository,
            TeamRepository teamRepository) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.goalRepository = goalRepository;
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<Player> getPlayer(@PathVariable("playerId") Long playerId) {
        return ResponseEntity.of(playerRepository.findById(playerId));
    }

    @GetMapping
    public Iterable<Player> getPlayers(@RequestParam(value = "teamId", required = false) Long teamId) {
        if(teamId != null){
            return playerRepository.findByTeamId(teamId);
        }else{
            return playerRepository.findAll();
        }
    }

    @PostMapping
    public Player createPlayers(@RequestBody Player player) throws TeamNotFoundException {
        // Make sure id is null to avoid update of existing player
        player.setId(null);

        if(player.getTeamId() != null){
            Optional<Team> existingTeamOptional = teamRepository.findById(player.getTeamId());
            if(existingTeamOptional.isPresent()){
                player.setTeam(existingTeamOptional.get());
            } else{
                throw new TeamNotFoundException();
            }

        }

        return playerRepository.save(player);
    }

    @PutMapping
    public Player putPlayer(@RequestBody Player player) {
        Optional<Player> existingPlayerOptional = playerRepository.findById(player.getId());

        if(existingPlayerOptional.isPresent()) {
            Player existingPlayer = existingPlayerOptional.get();

            Team playerTeam = getPlayerTeam(player);

            // In case player type is changed, player migration needs to be done because
            // it's not same type of player and new player needs to be created.
            if(!existingPlayer.getType().equals(player.getType())) {
                // If player team is provided, set it to new player, if not set team from existing player (if it exist).
                if(playerTeam != null) {
                    player.setTeam(playerTeam);
                } else {
                    player.setTeam(existingPlayer.getTeam());
                }

                // Do player migration and return new version of player
                return migratePlayer(existingPlayer, player);
            } else {
                // Update provided fields on existing player including team if it's provided
                if(player.getNumberoOnDress() != null) {
                    existingPlayer.setNumberoOnDress(player.getNumberoOnDress());
                }

                if(player.getFirstName() != null) {
                    existingPlayer.setFirstName(player.getFirstName());
                }

                if(player.getLastName() != null) {
                    existingPlayer.setLastName(player.getLastName());
                }

                if(player.getPosition() != null) {
                    existingPlayer.setPosition(player.getPosition());
                }

                if (playerTeam != null) {
                    existingPlayer.setTeam(playerTeam);
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
     * Method returns player team based on provided player. It checks if teamId exists on player or if team exist.
     * If team with provided id doesn't exist method throws TeamNotFoundException.
     *
     * @param player
     * @return
     */
    private Team getPlayerTeam(Player player) {
        Long teamId = player.getTeamId() != null ? player.getTeamId() :
                (player.getTeam() != null ? player.getTeam().getId() : null);

        if(teamId != null) {
            Optional<Team> existingTeamOptional = teamRepository.findById(teamId);

            if (existingTeamOptional.isPresent()) {
                return existingTeamOptional.get();
            } else {
                throw new TeamNotFoundException();
            }
        } else {
            return null;
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

        // Save new player
        newPlayer = playerRepository.save(newPlayer);

        // Move all goals to new version of player
        List<Goal> goals = goalRepository.findByPlayer(oldPlayer);

        for(Goal goal : goals) {
            goal.setPlayer(newPlayer);
            goalRepository.save(goal);
        }

        // Remove old player
        playerRepository.delete(oldPlayer);

        return newPlayer;
    }
}