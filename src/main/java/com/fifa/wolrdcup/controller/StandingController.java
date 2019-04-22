package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.model.*;
import com.fifa.wolrdcup.repository.MatchRepository;
import com.fifa.wolrdcup.repository.TeamRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
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

            //TODO: These 5 lines should be removed and changed with setTeamStats(team, value)
            //TODO: getGoalsScore, getGoalsConceded, getWon, getDraw and getLose methods should be removed
            value.setGoalsScored(getGoalsScored(team));
            value.setGoalsConceded(getGoalsConceded(team));
            value.setLose(getLose(team));
            value.setWon(getWon(team));
            value.setDraw(getDraw(team));

            result.add(value);
        }

        // TODO: Remember well how we're sorting list by using multiple criterias!
        result.sort((StandingValue o1, StandingValue o2) -> {
            int value = (int)(o2.getPoints()-o1.getPoints());

            // If points are equal, than compare goals difference
            if(value == 0) {
                value = (int)(o2.getGoalsDifference()-o1.getGoalsDifference());
            }

            // If points and goals difference are equal, than compare goals scored
            if(value == 0) {
                value = (int)(o2.getGoalsScored()-o1.getGoalsScored());
            }

            return value;
        });
        return result;
    }

    private void setTeamStats(Team team, StandingValue value) {
        long goalsScored = 0L;
        long goalsConceded = 0L;
        long won = 0L;
        long lose = 0L;
        long draw = 0L;

        //TODO: Do implementation here

        value.setGoalsScored(goalsScored);
        value.setGoalsConceded(goalsConceded);
        value.setLose(lose);
        value.setWon(won);
        value.setDraw(draw);
    }

    private Long getGoalsScored(Team team) {
        long goalsScored = 0L;

        Iterable<Match> matches = matchRepository.findByTeam1OrTeam2(team, team);
        for(Match match : matches){

            if(team.getId().equals(match.getTeam1().getId())) {

                goalsScored += match.getScore1();
            }
            if(team.getId().equals(match.getTeam2().getId())){
                goalsScored += match.getScore2();
            }
        }

        return goalsScored;
    }

    private Long getGoalsConceded(Team team) {
        long goalsConceded = 0L;

        Iterable<Match> matches = matchRepository.findByTeam1OrTeam2(team, team);
        for(Match match : matches){

            if(team.getId().equals(match.getTeam1().getId())) {

                goalsConceded += match.getScore2();
            }
            if(team.getId().equals(match.getTeam2().getId())){

                goalsConceded += match.getScore1();
            }
        }
        return goalsConceded;
    }

    private Long getWon(Team team) {
        long won = 0L;

        Iterable<Match> matches = matchRepository.findByTeam1OrTeam2(team, team);

        for(Match match : matches){

            if(team.getId().equals(match.getTeam1().getId())){

                if (match.getScore1() > match.getScore2()){
                    won += 1;
                }
            }else if((team.getId().equals(match.getTeam2().getId()))){

                if (match.getScore1() < match.getScore2()){
                    won += 1;
                }
            }
        }
        return won;
    }

    private Long getLose(Team team) {
        long lose = 0L;

        Iterable<Match> matches = matchRepository.findByTeam1OrTeam2(team, team);

        for(Match match : matches){

            if(team.getId().equals(match.getTeam1().getId())){

                if (match.getScore1() < match.getScore2()){
                    lose += 1;
                }

            }else if((team.getId().equals(match.getTeam2().getId()))){

                if (match.getScore1() > match.getScore2()){
                    lose += 1;
                }
            }
        }
        return lose;
    }

    private Long getDraw(Team team) {
        long draw = 0L;

        Iterable<Match> matches = matchRepository.findByTeam1OrTeam2(team, team);

         for(Match match : matches){

            if(team.getId().equals(match.getTeam1().getId())){

                if (match.getScore1().equals(match.getScore2())){
                    draw += 1;
                }

            }else if((team.getId().equals(match.getTeam2().getId()))){

                if (match.getScore1().equals(match.getScore2())){
                    draw += 1;
                }
            }
        }
        return draw;
    }
}
