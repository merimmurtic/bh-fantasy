package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.model.*;
import com.fifa.wolrdcup.repository.MatchRepository;
import com.fifa.wolrdcup.repository.TeamRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/standings")
public class StandingController {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    public StandingController(TeamRepository teamRepository, MatchRepository matchRepository) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
    }

    @GetMapping
    public Iterable<StandingValue> getStandings() {
        List<StandingValue> result = new ArrayList<>();

        Iterable<Team> teams = teamRepository.findAll();

        for(Team team : teams){
            StandingValue value = new StandingValue();
            value.setTeamId(team.getId());
            value.setTeamName(team.getName());
            value.setGoalsScored(getGoalsScored(team));
            value.setGoalsConceded(getGoalsConceded(team));
            value.setLose(getLose(team));
            value.setWon(getWon(team));
            value.setDraw(getDraw(team));

            result.add(value);
        }

        return result;
    }

    private Long getGoalsScored(Team team) {
        long goalsScored = 0L;

        Iterable<Match> matches = matchRepository.findByTeam1OrTeam2(team, team);
        for(Match match : matches){
            List<Goal> goals = match.getGoals();

            for(Goal goal : goals) {

                Long goalTeamId = goal.getPlayer().getTeamId();

                Long teamId = team.getId();

                if(!goal.getOwnGoal()){
                    if(goalTeamId.equals(teamId)) {
                        goalsScored += 1;
                    }
                } else{
                    if(!goalTeamId.equals(teamId)) {
                        goalsScored += 1;
                    }
                }
            }
        }
        return goalsScored;

    }

    private Long getGoalsConceded(Team team) {
        long goalsConceded = 0L;

        Iterable<Match> matches = matchRepository.findByTeam1OrTeam2(team, team);
        for(Match match : matches){
            List<Goal> goals = match.getGoals();

            for(Goal goal : goals){

                Long goalTeamId = goal.getPlayer().getTeamId();

                Long teamId = team.getId();

                if(goal.getOwnGoal()){
                    if(goalTeamId.equals(teamId)){
                     goalsConceded += 1;
                    }
                } else{
                    if(!goalTeamId.equals(teamId)){
                        goalsConceded += 1;
                    }
                }

            }
        }

        return goalsConceded;
    }

    private Long getWon(Team team) {
        long won = 0L;

        Iterable<Match> matches = matchRepository.findByTeam1OrTeam2(team, team);

        for(Match match : matches){

            List<Goal> golas = match.getGoals();


            for(Goal goal : golas){

                Long goalTeamId = goal.getPlayer().getTeamId();

                Long teamId = team.getId();

                String currentTeam = goal.getPlayer().getTeam().getName();

                if(goalTeamId.equals(teamId)) {
                    if(currentTeam.equals(match.getTeam1().getName())) {

                        if (goal.getScore1() > goal.getScore2()) {

                            won += 1;
                        }
                    } else if(currentTeam.equals(match.getTeam2().getName())){

                        if (goal.getScore2() > goal.getScore1()) {

                            won += 1;
                        }
                    }

                }
            }
        }


        return won;
    }

    private Long getLose(Team team) {
        long lose = 0L;

        return lose;
    }

    private Long getDraw(Team team) {
        long draw = 0L;

        return draw;
    }
}
