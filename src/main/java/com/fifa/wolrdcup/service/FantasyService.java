package com.fifa.wolrdcup.service;


import com.fifa.wolrdcup.exception.InvalidLeagueIdException;
import com.fifa.wolrdcup.model.*;
import com.fifa.wolrdcup.model.custom.PointsValue;
import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.model.league.RegularLeague;
import com.fifa.wolrdcup.repository.LeagueRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class FantasyService {

    private final LeagueRepository leagueRepository;

    public FantasyService(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @Transactional
    public void process(Long leagueId) {
        Optional<League> leagueOptional = leagueRepository.findById(leagueId);

        if(!leagueOptional.isPresent() || !(leagueOptional.get() instanceof RegularLeague)) {
            throw new InvalidLeagueIdException();
        }

        RegularLeague league = (RegularLeague) leagueOptional.get();

        for(Round round : league.getRounds()) {
            for(Match match : round.getMatches()) {
                if(match.getScore1() == null) {
                    continue;
                }

                Map<Long, PointsValue> pointsMap = calculatePointsForMatch(match);

                //TODO: Merim, save points here
            }
        }
    }

    public Map<Long, PointsValue> calculatePointsForMatch(Match match) {
        Map<Long, PointsValue> pointsMap = new HashMap<>();

        for(Goal goal : match.getGoals()) {
            pointsMap.putIfAbsent(goal.getPlayer().getId(), new PointsValue(goal.getPlayer()));

            if(!goal.getOwnGoal()) {
                pointsMap.get(goal.getPlayer().getId()).addGoal();
            } else {
                pointsMap.get(goal.getPlayer().getId()).addOwnGoal();
            }

            if(goal.getAssist() != null) {
                pointsMap.putIfAbsent(goal.getAssist().getId(), new PointsValue(goal.getAssist()));

                pointsMap.get(goal.getAssist().getId()).addAssist();
            }
        }

        //TODO: Merim, add all other points (feel free to update PointsValue)

        return pointsMap;
    }
}
