package com.fifa.wolrdcup.service;


import com.fifa.wolrdcup.exception.InvalidLeagueIdException;
import com.fifa.wolrdcup.model.*;
import com.fifa.wolrdcup.model.custom.PointsValue;
import com.fifa.wolrdcup.model.league.FantasyLeague;
import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.model.league.RegularLeague;
import com.fifa.wolrdcup.repository.FantasyLineupRepository;
import com.fifa.wolrdcup.repository.LeagueRepository;
import com.fifa.wolrdcup.repository.LineupRepository;
import com.fifa.wolrdcup.repository.TeamRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class FantasyService {

    private final LeagueRepository leagueRepository;

    private final TeamRepository teamRepository;

    private final FantasyLineupRepository fantasyLineupRepository;

    private final LineupRepository lineupRepository;

    public FantasyService(LeagueRepository leagueRepository, TeamRepository teamRepository,
                          FantasyLineupRepository fantasyLineupRepository, LineupRepository lineupRepository) {
        this.leagueRepository = leagueRepository;
        this.teamRepository = teamRepository;
        this.fantasyLineupRepository = fantasyLineupRepository;
        this.lineupRepository = lineupRepository;
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

    @Transactional
    public void seedFantasyPlayerLeague(Long leagueId) {
        FantasyLeague fantasyLeague = new FantasyLeague();
        fantasyLeague.setName("Fantasy Premijer Liga");

        leagueRepository.findById(leagueId).ifPresent((league -> {
            fantasyLeague.setRegularLeague((RegularLeague) league);
        }));

        leagueRepository.save(fantasyLeague);

        Team fantasyTeam = new Team();
        fantasyTeam.setCode("VUCKO");
        fantasyTeam.setName("Vucko");

        teamRepository.save(fantasyTeam);

        FantasyLineup fantasyLineup = new FantasyLineup();
        fantasyLineup.setLeague(fantasyLeague);
        fantasyLineup.setTeam(fantasyTeam);
        fantasyLineup.setRound(fantasyLeague.getRegularLeague().getRounds().get(0));

        Lineup lineup = new Lineup();
        lineup.setFormation(Lineup.Formation.F_4_3_3);

        lineupRepository.save(lineup);

        fantasyLineup.setLineup(lineup);

        fantasyLineupRepository.save(fantasyLineup);
    }
}
