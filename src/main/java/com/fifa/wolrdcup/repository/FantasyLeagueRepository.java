package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.league.FantasyLeague;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FantasyLeagueRepository extends CrudRepository<FantasyLeague, Long> {
    Optional<FantasyLeague> findByRegularLeague_Id(Long id);
}
