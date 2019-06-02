package com.bhfantasy.web.repository;

import com.bhfantasy.web.model.Goal;
import com.bhfantasy.web.model.players.Player;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GoalRepository extends CrudRepository<Goal, Long> {

    List<Goal> findByPlayer(Player player);

    Long countGoalsByPlayer(Player player);
}
