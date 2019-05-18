package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Match;
import com.fifa.wolrdcup.model.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends CrudRepository<Match, Long> {
    @EntityGraph(value = "Match.standings", type = EntityGraph.EntityGraphType.LOAD)
    Iterable<Match> getByTeam1OrTeam2(Team team1, Team team2);

    @EntityGraph(value = "Match.detail", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Match> getDistinctByIdAndRounds_IdAndRounds_League_Id(Long id, Long roundId, Long leagueId);

    Optional<Match> findByTransfermarktId(Long id);

    @Query("select match " +
            "from RegularLeague regularLeague " +
            "join regularLeague.groups leagueGroup " +
            "join leagueGroup.rounds round " +
            "join round.matches match " +
            "where regularLeague.id = :leagueId and regularLeague.dtype = 'RegularLeague' " +
            "order by match.dateTime asc")
    List<Match> findMultiLeagueMatches(@Param("leagueId") Long leagueId);
}
