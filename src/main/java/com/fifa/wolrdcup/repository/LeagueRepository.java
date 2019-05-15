package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.custom.TopPlayerValue;
import com.fifa.wolrdcup.model.league.League;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeagueRepository extends CrudRepository<League, Long> {
    Optional<League> findByName(String name);

    @EntityGraph(value = "League.detail", type = EntityGraph.EntityGraphType.LOAD)
    Optional<League> getById(Long aLong);

    @Query("select new com.fifa.wolrdcup.model.custom.TopPlayerValue(" +
            "player.id, trim(concat(coalesce(player.firstName, ''), ' ', coalesce(player.lastName, ''))), count(goal)) " +
            "from Goal goal " +
            "join goal.player player " +
            "join player.teams teams " +
            "join teams.leagues leagues " +
            "where leagues.id = :leagueId " +
            "group by player order by count(goal) desc")
    List<TopPlayerValue> getTopPlayers(@Param("leagueId") Long leagueId);
}
