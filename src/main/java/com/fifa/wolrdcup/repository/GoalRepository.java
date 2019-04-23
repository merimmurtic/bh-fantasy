package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Goal;
import com.fifa.wolrdcup.model.players.Player;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GoalRepository extends CrudRepository<Goal, Long> {

    List<Goal> findByPlayer(Player player);

    // TODO: This should be called findGoalsByPlayer, however,
    // instead of using this method you should use countGoalsByPlayer,
    // which will return number of records find in database


    Long countGoalsByPlayer(Player player);
}
