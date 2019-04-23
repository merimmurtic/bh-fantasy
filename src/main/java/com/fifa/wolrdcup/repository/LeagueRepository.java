package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.League;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LeagueRepository extends CrudRepository<League, Long> {
}
