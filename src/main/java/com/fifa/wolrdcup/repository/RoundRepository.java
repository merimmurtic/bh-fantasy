package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Round;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RoundRepository extends CrudRepository<Round, Long> {
    List<Round> findByNameContaining(String name);

    Iterable<Round> findByLeagueId(Long leagueId);

    Optional<Round> findByIdAndLeague_Id(Long id, Long leagueId);

    Optional<Round> findByLeagueIdAndName(Long leagueId, String name);
}
