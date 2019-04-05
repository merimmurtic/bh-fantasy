package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.exception.PlayerNotFoundException;
import com.fifa.wolrdcup.exception.TeamNotFoundException;
import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.model.players.Defender;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.repository.PlayerRepository;
import com.fifa.wolrdcup.repository.TeamRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/players")
public class PlayerController {
    private final PlayerRepository playerRepository;

    private final TeamRepository teamRepository;

    public PlayerController(
            PlayerRepository playerRepository,
            TeamRepository teamRepository) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<Player> getPlayer(@PathVariable("playerId") Long playerId) throws Exception {

        return ResponseEntity.of(playerRepository.findById(playerId));
    }

    @GetMapping
    public Iterable<Player> getPlayers(@RequestParam(value = "teamId", required = false) Long teamId) throws Exception{
        if(teamId != null){
            return playerRepository.findByTeamId(teamId);
        }else{
            return playerRepository.findAll();
        }
    }

    @PutMapping
    public Player putPlayer(@RequestBody Player player) throws Exception {
        Optional<Player> existingPlayerOptional = playerRepository.findById(player.getId());

        if(existingPlayerOptional.isPresent()) {
            Player existingPlayer = existingPlayerOptional.get();
            existingPlayer.setNumberoOnDress(player.getNumberoOnDress());
            existingPlayer.setFirstName(player.getFirstName());
            existingPlayer.setLastName(player.getLastName());
            existingPlayer.setPosition(player.getPosition());
            existingPlayer.setType(player.getClass().getSimpleName());

            if(player.getTeamId() != null) {
                Optional<Team> existingTeamOptional = teamRepository.findById(player.getTeamId());

                if(existingTeamOptional.isPresent()) {
                    existingPlayer.setTeam(existingTeamOptional.get());
                } else {
                    throw new TeamNotFoundException();
                }
            }

            return playerRepository.save(existingPlayer);
        } else {
            throw new PlayerNotFoundException();
        }
    }
}