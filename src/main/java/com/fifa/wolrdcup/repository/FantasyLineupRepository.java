package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.FantasyLineup;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FantasyLineupRepository extends CrudRepository<FantasyLineup, Long> {
    Optional<FantasyLineup> findByIdAndLeague_IdAndTeam_IdAndRound_Id(
            Long fantasyLineupId, Long leagueId, Long teamId, Long roundId);
}
