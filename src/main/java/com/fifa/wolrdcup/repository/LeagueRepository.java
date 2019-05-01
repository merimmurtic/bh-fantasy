package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.league.League;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface LeagueRepository extends CrudRepository<League, Long> {

}
