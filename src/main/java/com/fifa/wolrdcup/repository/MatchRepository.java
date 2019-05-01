package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Match;
import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.model.players.Player;
import org.springframework.data.repository.CrudRepository;

public interface MatchRepository extends CrudRepository<Match, Long> {
    Iterable<Match> findByTeam1OrTeam2(Team team1, Team team2);

    Iterable<Match> findByIdAndRound_IdAndRound_League_Id(Long id, Long roundId, Long leagueId);
}
