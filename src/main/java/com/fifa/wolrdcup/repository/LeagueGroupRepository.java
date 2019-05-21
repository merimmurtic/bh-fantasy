package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.league.LeagueGroup;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeagueGroupRepository extends CrudRepository<LeagueGroup, Long> {
    @Query("select distinct g from RegularLeague league " +
            "inner join league.groups g " +
            "inner join fetch g.teams team where league.id = :leagueId")
    List<LeagueGroup> getGroupsWithTeams(@Param("leagueId") Long leagueId);

    @Query("select g from RegularLeague league " +
            "inner join league.groups g " +
            "where league.id = :leagueId and g.name = :name")
    Optional<LeagueGroup> findGroup(@Param("leagueId") Long leagueId, @Param("name") String name);
}
