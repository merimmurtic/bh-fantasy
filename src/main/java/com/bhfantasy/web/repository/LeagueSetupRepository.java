package com.bhfantasy.web.repository;

import com.bhfantasy.web.model.LeagueSetup;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface LeagueSetupRepository extends CrudRepository<LeagueSetup, Long> {

    @Query("select distinct leagueSetup from LeagueSetup leagueSetup " +
            "join leagueSetup.league league " +
            "join league.rounds round " +
            "join round.matches match " +
            "where leagueSetup.transfermarktUrl is not null and match.dateTime > :today and match.dateTime < :tomorrow " +
            "order by leagueSetup.id")
    Iterable<LeagueSetup> findLeaguesBetween(@Param("today") LocalDateTime today, @Param("tomorrow") LocalDateTime tomorrow);
}
