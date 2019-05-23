package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Team;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TeamRepository extends CrudRepository<Team, Long> {
    List<Team> findByNameContaining(String name);

    Optional<Team> findByCode(String code);

    List<Team> findByLeagues_Id(Long leagueId);

    @Query("select team " +
            "from Team team " +
            "join team.players player " +
            "join fetch team.leagues league " +
            "where player.id = :playerId and league.dtype = 'RegularLeague' " +
            "order by team.id asc")
    Set<Team> getTeams(@Param("playerId") Long playerId);
}
