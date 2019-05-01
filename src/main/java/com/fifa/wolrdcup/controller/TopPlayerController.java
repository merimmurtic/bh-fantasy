package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.model.custom.TopPlayerValue;
import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.repository.GoalRepository;
import com.fifa.wolrdcup.repository.LeagueRepository;
import com.fifa.wolrdcup.repository.PlayerRepository;
import com.fifa.wolrdcup.repository.TeamRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("leagues/{leagueId}/top-players")
public class TopPlayerController {

    private final PlayerRepository playerRepository;
    private final GoalRepository goalRepository;

    public TopPlayerController(PlayerRepository playerRepository, GoalRepository goalRepository) {
        this.playerRepository = playerRepository;
        this.goalRepository = goalRepository;
    }

    @GetMapping
    public List<TopPlayerValue> getTopPlayers(
            @PathVariable("leagueId") Long leagueId) {

        List<TopPlayerValue> result = new ArrayList<>();
        Iterable<Player> players = playerRepository.findByTeams_Leagues_Id(leagueId);

        for (Player player : players) {

            TopPlayerValue stats = new TopPlayerValue();
            stats.setPlayerId(player.getId());
            stats.setFullName(player.getFullName());
            stats.setGoalsScored(getGoalsScored(player));

            result.add(stats);
        }

        result.sort((TopPlayerValue player1, TopPlayerValue player2)->
                (int) (player2.getGoalsScored()-player1.getGoalsScored()));
        return result;
    }
    private Long getGoalsScored(Player player) {
        return goalRepository.countGoalsByPlayer(player);
    }
}
