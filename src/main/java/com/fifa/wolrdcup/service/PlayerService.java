package com.fifa.wolrdcup.service;

import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional
    public Player processPlayer(Player player, Team team) {
        Optional<Player> existingPlayer = Optional.empty();

        if(player.getTransferMarktId() != null) {
            existingPlayer = playerRepository.findByTransferMarktId(player.getTransferMarktId());
        } else {
            if (player.getFirstName() != null) {
                existingPlayer = playerRepository.findByTeamsAndFirstNameAndLastName(
                        team, player.getFirstName(), player.getLastName());
            }

            if (!existingPlayer.isPresent()) {
                existingPlayer = playerRepository.findByTeamsAndLastName(
                        team, player.getLastName());
            }
        }

        existingPlayer.ifPresent((p) -> {
            boolean updated = false;

            if(player.getFirstName() != null && p.getFirstName() == null) {
                p.setFirstName(player.getFirstName());
                updated = true;
            }

            if(player.getTransferMarktId() != null && p.getTransferMarktId() == null) {
                p.setTransferMarktId(player.getTransferMarktId());
                updated = true;
            }

            if(team != null && !p.getTeams().stream().map(Team::getId)
                    .collect(Collectors.toSet()).contains(team.getId())) {
                p.getTeams().add(team);
                updated = true;
            }

            if(updated) {
                playerRepository.save(p);
            }
        });

        return existingPlayer.orElseGet(() -> {
            player.getTeams().add(team);
            return playerRepository.save(player);
        });
    }

    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }
}