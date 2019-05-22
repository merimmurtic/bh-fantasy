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
    public Optional<Player> getPlayer(Long transfermarktId) {
        return playerRepository.findByTransferMarktId(transfermarktId);
    }

    @Transactional
    public Player processPlayer(Player player, Team team) {
        Optional<Player> existingPlayerOptional = Optional.empty();

        if(player.getTransferMarktId() != null) {
            existingPlayerOptional = playerRepository.findByTransferMarktId(player.getTransferMarktId());
        }

        if(!existingPlayerOptional.isPresent()) {
            if (player.getFirstName() != null) {
                existingPlayerOptional = playerRepository.findByFirstNameAndLastName(
                        player.getFirstName(), player.getLastName());
            }

            if (!existingPlayerOptional.isPresent()) {
                existingPlayerOptional = playerRepository.findByLastName(player.getLastName());
            }
        }

        Player existingPlayer = null;

        if (existingPlayerOptional.isPresent()) {
            existingPlayer = existingPlayerOptional.get();
        }

        if(existingPlayer != null) {
            boolean updated = false;

            if (player.getFirstName() != null && !player.getFirstName().equals(existingPlayer.getFirstName())) {
                existingPlayer.setFirstName(player.getFirstName());
                updated = true;
            }

            if (player.getLastName() != null && !player.getLastName().equals(existingPlayer.getLastName())) {
                existingPlayer.setLastName(player.getLastName());
                updated = true;
            }

            if (player.getTransferMarktId() != null && existingPlayer.getTransferMarktId() == null) {
                existingPlayer.setTransferMarktId(player.getTransferMarktId());
                updated = true;
            }

            if (team != null && !existingPlayer.getTeams().stream().map(Team::getId)
                    .collect(Collectors.toSet()).contains(team.getId())) {
                existingPlayer.getTeams().add(team);
                updated = true;
            }

            if (player.getNumberoOnDress() != null && !player.getNumberoOnDress().equals(existingPlayer.getNumberoOnDress())) {
                existingPlayer.setNumberoOnDress(player.getNumberoOnDress());
                updated = true;
            }

            if (player.getBirthDate() != null && !player.getBirthDate().equals(existingPlayer.getBirthDate())) {
                existingPlayer.setBirthDate(player.getBirthDate());
                updated = true;
            }

            if (player.getMarketValueRaw() != null && !player.getMarketValueRaw().equals(existingPlayer.getMarketValueRaw())) {
                existingPlayer.setMarketValueRaw(player.getMarketValueRaw());
                updated = true;
            }

            if (player.getProfilePicture() != null && !player.getProfilePicture().equals(existingPlayer.getProfilePicture())) {
                existingPlayer.setProfilePicture(player.getProfilePicture());
                updated = true;
            }

            if (player.getType() != null && player.getType().equals(existingPlayer.getType())
                    && player.getPosition() != null && !player.getPosition().equals(existingPlayer.getPosition())) {
                existingPlayer.setPosition(player.getPosition());
                updated = true;
            }

            if (updated) {
                existingPlayer = playerRepository.save(existingPlayer);
            }
        } else {
            existingPlayer = playerRepository.save(player);

            existingPlayer.getTeams().add(team);
        }

        return existingPlayer;
    }

    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }
}
