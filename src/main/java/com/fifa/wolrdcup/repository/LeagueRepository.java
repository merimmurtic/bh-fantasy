package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.league.League;
import org.springframework.data.repository.CrudRepository;


public interface LeagueRepository extends CrudRepository<League, Long> {
}
