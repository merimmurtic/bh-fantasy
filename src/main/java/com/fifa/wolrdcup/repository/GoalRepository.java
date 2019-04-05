package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Goal;
import com.fifa.wolrdcup.model.Team;
import org.springframework.data.repository.CrudRepository;

public interface GoalRepository extends CrudRepository<Goal, Long> {
    Long countGoalsByMatch_Team1AndMatch_Team2(Team team1, Team team2);
}
