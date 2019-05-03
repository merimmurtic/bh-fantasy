package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.model.players.Player;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends CrudRepository<Player, Long> {
    Optional<Player> findByTeamsAndFirstNameAndLastName(Team team, String firstName, String lastName);

    Optional<Player> findByTeamsAndLastName(Team team, String lastName);

    Optional<Player> findByTransferMarktId(Long transferMarktId);

    List<Player> findByTeams(Long teamId);

    List<Player> findDistinctByTeams_Leagues_Id(Long leagueId);

}
