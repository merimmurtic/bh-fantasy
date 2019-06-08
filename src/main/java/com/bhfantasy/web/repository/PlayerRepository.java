package com.bhfantasy.web.repository;

import com.bhfantasy.web.model.players.Player;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends CrudRepository<Player, Long> {
    Iterable<Player> findByFirstNameAndLastName(String firstName, String lastName);

    Iterable<Player> findByLastName(String lastName);

    Optional<Player> findByTransferMarktId(Long transferMarktId);

    List<Player> findByTeams(Long teamId);

    List<Player> findByTeamsAndTeams_Leagues_Id(Long teamId, Long leagueId);

    Optional<Player> findByIdAndTeams_Leagues_Id(Long id, Long leagueId);

    @Query("select distinct player from Player player " +
            "join fetch player.teams team " +
            "join team.leagues league " +
            "where league.id = :leagueId and league.dtype = 'RegularLeague' order by player.id asc")
    List<Player> findPlayersWithTeams(@Param("leagueId") Long leagueId);
}
