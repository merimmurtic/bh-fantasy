package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.model.Goal;
import com.fifa.wolrdcup.model.Match;
import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.model.TopPlayerValue;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.model.players.Unknown;
import com.fifa.wolrdcup.repository.GoalRepository;
import com.fifa.wolrdcup.repository.MatchRepository;
import com.fifa.wolrdcup.repository.PlayerRepository;
import com.fifa.wolrdcup.repository.TeamRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/topplayers")
public class TopPlayerController {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final GoalRepository goalRepository;

    public TopPlayerController(PlayerRepository playerRepository, TeamRepository teamRepository, MatchRepository matchRepository, GoalRepository goalRepository) {
        this.playerRepository = playerRepository;
        this.goalRepository = goalRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;

    }

    @GetMapping
    public List<TopPlayerValue> getTopPlayers() {
        List<TopPlayerValue> result = new ArrayList<>();

        Iterable<Player> players = playerRepository.findAll();
        Iterable<Team> teams = teamRepository.findAll();

        for(Team team : teams) {

            for (Player player : players) {
                TopPlayerValue stats = new TopPlayerValue();
                stats.setPlayerId(player.getId());
                stats.setFullName(player.getFullName());
                stats.setGoalsScored(getGoalsScored(player));
                if(stats.getGoalsScored() != null){
                    stats.setGoalsScored(getGoalsScored(player));
                }

                result.add(stats);
            }
        }

        result.sort((TopPlayerValue player1, TopPlayerValue player2)-> (int) (player2.getGoalsScored()-player1.getGoalsScored()));


        return result;
    }

    private Long getGoalsScored(Player player) {
        long goalsScored = 0L;

        Iterable<Goal> goals = goalRepository.findGoalByPlayer(player);

        for (Goal goal : goals) {

            if (player.getId().equals(goal.getPlayer().getId())) {

                goalsScored += 1;
            }
        }

        return goalsScored;
    }
}
