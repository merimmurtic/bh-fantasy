package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Goal;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.model.players.Unknown;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GoalRepository extends CrudRepository<Goal, Long> {

    List<Goal> findByPlayer(Player player);

    Iterable<Goal> findGoalByPlayer(Player player);


}
