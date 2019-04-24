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

            // TODO: Think about this and let me know if something is not clear, this is basic thing.
            // value is provided to method where it is updated with stats
            setTeamStats(team, value);

            result.add(value);
        }

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

        Iterable<Match> matches = matchRepository.findByTeam1OrTeam2(team, team);

        for(Match match : matches){

            // Only check one time if scores are equal and increase counter if they are
            if (match.getScore1().equals(match.getScore2())){
                draw += 1;
            }

            if(team.getId().equals(match.getTeam1().getId())) {
                if (match.getScore1() < match.getScore2()){
                    lose += 1;
                } else if (match.getScore1() > match.getScore2()){
                    won += 1;
                }

                goalsScored += match.getScore1();
                goalsConceded += match.getScore2();
            } else if(team.getId().equals(match.getTeam2().getId())){
                if (match.getScore1() > match.getScore2()){
                    lose += 1;
                } else if (match.getScore1() < match.getScore2()){
                    won += 1;
                }

                goalsScored += match.getScore2();
                goalsConceded += match.getScore1();
            }
        }

        value.setGoalsScored(goalsScored);
        value.setGoalsConceded(goalsConceded);
        value.setLose(lose);
        value.setWon(won);
        value.setDraw(draw);
    }
}
