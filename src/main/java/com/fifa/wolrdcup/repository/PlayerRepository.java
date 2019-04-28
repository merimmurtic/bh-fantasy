package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.model.players.Player;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends CrudRepository<Player, Long> {

    Optional<Player> findByTeamAndFirstNameAndLastName(Team team, String firstName, String lastName);



    List<Player> findByTeamId(Long teamId);


}
