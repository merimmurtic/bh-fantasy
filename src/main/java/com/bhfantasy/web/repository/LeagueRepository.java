package com.bhfantasy.web.repository;

import com.bhfantasy.web.model.league.League;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface LeagueRepository extends CrudRepository<League, Long> {
    Optional<League> findByName(String name);

    @EntityGraph(value = "League.detail", type = EntityGraph.EntityGraphType.LOAD)
    Optional<League> getById(Long aLong);
}
