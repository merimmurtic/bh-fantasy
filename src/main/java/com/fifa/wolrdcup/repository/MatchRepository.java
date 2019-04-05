package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Match;
import org.springframework.data.repository.CrudRepository;

public interface MatchRepository extends CrudRepository<Match, Long> {
}
