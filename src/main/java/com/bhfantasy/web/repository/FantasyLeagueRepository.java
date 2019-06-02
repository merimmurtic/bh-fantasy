package com.bhfantasy.web.repository;

import com.bhfantasy.web.model.league.FantasyLeague;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FantasyLeagueRepository extends CrudRepository<FantasyLeague, Long> {
    Optional<FantasyLeague> findByRegularLeague_Id(Long id);
}
