package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.model.Round;
import com.fifa.wolrdcup.model.players.Defender;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.repository.PlayerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players")
public class PlayerController {
    private final PlayerRepository playerRepository;

    public PlayerController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
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
    public Player putPlayer(@RequestBody Defender player) {
        return playerRepository.save(player);
    }
}