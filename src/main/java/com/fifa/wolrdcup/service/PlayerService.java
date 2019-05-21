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
                existingPlayer = playerRepository.findByFirstNameAndLastName(
                        player.getFirstName(), player.getLastName());
            }

            if (!existingPlayer.isPresent()) {
                existingPlayer = playerRepository.findByLastName(player.getLastName());
            }
        }

        existingPlayer.ifPresent((p) -> {
            boolean updated = false;

            if(player.getFirstName() != null && !player.getFirstName().equals(p.getFirstName())) {
                p.setFirstName(player.getFirstName());
                updated = true;
            }

            if(player.getLastName() != null && !player.getLastName().equals(p.getLastName())) {
                p.setLastName(player.getLastName());
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

            if(player.getNumberoOnDress() != null && !player.getNumberoOnDress().equals(p.getNumberoOnDress())) {
                p.setNumberoOnDress(player.getNumberoOnDress());
                updated = true;
            }

            if(player.getBirthDate() != null && !player.getBirthDate().equals(p.getBirthDate())) {
                p.setBirthDate(player.getBirthDate());
                updated = true;
            }

            if(player.getMarketValueRaw() != null && !player.getMarketValueRaw().equals(p.getMarketValueRaw())) {
                p.setMarketValueRaw(player.getMarketValueRaw());
                updated = true;
            }

            if(player.getProfilePicture() != null && !player.getProfilePicture().equals(p.getProfilePicture())) {
                p.setProfilePicture(player.getProfilePicture());
                updated = true;
            }

            if(player.getPosition() != null && !player.getPosition().equals(p.getPosition())) {
                p.setPosition(player.getPosition());
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
