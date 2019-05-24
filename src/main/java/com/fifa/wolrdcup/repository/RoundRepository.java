package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Round;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoundRepository extends CrudRepository<Round, Long> {

    Iterable<Round> findByLeagueIdOrderById(Long leagueId);

    @EntityGraph(value = "Round.detail", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Round> getByIdAndLeague_Id(Long id, Long leagueId);

    Optional<Round> findByLeagueIdAndName(Long leagueId, String name);

    Optional<Round> findFirstByLeagueIdAndMatches_Score1IsNotNullOrderByMatches_DateTimeDesc(Long leagueId);
}
