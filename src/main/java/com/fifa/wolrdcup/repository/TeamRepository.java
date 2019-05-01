package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.model.Team;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends CrudRepository<Team, Long> {
    List<Team> findByNameContaining(String name);

    Optional<Team> findByCodeAndLeagues(String code, League league);

    List<Team> findByLeagues_Id(Long id);
}