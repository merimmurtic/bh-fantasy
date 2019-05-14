package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Match;
import com.fifa.wolrdcup.model.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MatchRepository extends CrudRepository<Match, Long> {
    Iterable<Match> findByTeam1OrTeam2(Team team1, Team team2);

    @EntityGraph(value = "Match.detail", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Match> getByIdAndRound_IdAndRound_League_Id(Long id, Long roundId, Long leagueId);

    Optional<Match> findByTransfermarktId(Long id);
}
