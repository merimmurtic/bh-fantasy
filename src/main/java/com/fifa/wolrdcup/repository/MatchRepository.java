package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Match;
import com.fifa.wolrdcup.model.Team;
import org.springframework.data.repository.CrudRepository;

public interface MatchRepository extends CrudRepository<Match, Long> {
    Iterable<Match> findByTeam1OrTeam2(Team team1, Team team2);
}
