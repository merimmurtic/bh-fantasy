package com.bhfantasy.web.repository;

import com.bhfantasy.web.model.FantasyLineup;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FantasyLineupRepository extends CrudRepository<FantasyLineup, Long> {
    Optional<FantasyLineup> findByIdAndLeague_IdAndTeam_IdAndRound_Id(
            Long fantasyLineupId, Long leagueId, Long teamId, Long roundId);
}
