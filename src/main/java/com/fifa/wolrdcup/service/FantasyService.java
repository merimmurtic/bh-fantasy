package com.fifa.wolrdcup.service;


import com.fifa.wolrdcup.exception.InvalidLeagueIdException;
import com.fifa.wolrdcup.model.*;
import com.fifa.wolrdcup.model.custom.PointsValue;
import com.fifa.wolrdcup.model.league.FantasyLeague;
import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.model.league.RegularLeague;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.repository.*;
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

    private final PlayerRepository playerRepository;

    private final PlayerPointsRepository playerPointsRepository;

    public FantasyService(PlayerPointsRepository playerPointsRepository,
                          LeagueRepository leagueRepository, TeamRepository teamRepository,
                          FantasyLineupRepository fantasyLineupRepository,
                          LineupRepository lineupRepository, PlayerRepository playerRepository) {
        this.leagueRepository = leagueRepository;
        this.teamRepository = teamRepository;
        this.fantasyLineupRepository = fantasyLineupRepository;
        this.lineupRepository = lineupRepository;
        this.playerRepository = playerRepository;
        this.playerPointsRepository = playerPointsRepository;
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

                for(Long playerId : pointsMap.keySet()) {
                    PointsValue pointsValue = pointsMap.get(playerId);

                    PlayerPoints playerPoints = new PlayerPoints();
                    playerPoints.setMatch(match);
                    playerPoints.setPlayer(pointsValue.getPlayer());
                    playerPoints.setPoints(pointsValue.getTotalPoints());

                    playerPointsRepository.save(playerPoints);
                }


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

        for(Card card : match.getCards()) {
            pointsMap.putIfAbsent(card.getPlayer().getId(), new PointsValue(card.getPlayer()));

            if(card.getCardType() == Card.CardType.RED) {
                pointsMap.get(card.getPlayer().getId()).addRedCard();
            } else if(card.getCardType() == Card.CardType.YELLOW) {
                pointsMap.get(card.getPlayer().getId()).addYellowCard();
            }
        }

        for(Player player : match.getLineup1().getStartingPlayers()){
            pointsMap.putIfAbsent(player.getId(), new PointsValue(match.getLineup1().getCapiten()));

            if(player.getId().equals(match.getLineup1().getCapiten().getId())){

                pointsMap.get(match.getLineup1().getId()).addCapiten();
            }

            if(player.getId().equals(match.getLineup1().getViceCapiten().getId())){
                pointsMap.putIfAbsent(player.getId(), new PointsValue(match.getLineup1().getViceCapiten()));
                pointsMap.get(match.getLineup1().getId()).addViceCapiten();
            }


        }

        for(Player player : match.getLineup2().getStartingPlayers()){
            pointsMap.putIfAbsent(player.getId(), new PointsValue(match.getLineup2().getCapiten()));

            if(player.getId().equals(match.getLineup2().getCapiten().getId())){

                pointsMap.get(match.getLineup2().getId()).addCapiten();
            }

            if(player.getId().equals(match.getLineup2().getViceCapiten().getId())){
                pointsMap.putIfAbsent(player.getId(), new PointsValue(match.getLineup1().getViceCapiten()));
                pointsMap.get(match.getLineup2().getId()).addViceCapiten();
            }

        }

        for(Substitution substitution : match.getLineup1().getSubstitutionChanges()){

            pointsMap.putIfAbsent(substitution.getLineup().getId(), new PointsValue(substitution.getSubstitutePlayer()));

            pointsMap.get(substitution.getSubstitutePlayer().getId()).addMinutesPlayed(90 - substitution.getMinute());
        }

        for(Substitution substitution : match.getLineup2().getSubstitutionChanges()){

            pointsMap.putIfAbsent(substitution.getLineup().getId(), new PointsValue(substitution.getSubstitutePlayer()));

            pointsMap.get(substitution.getSubstitutePlayer().getId()).addMinutesPlayed(90 - substitution.getMinute());
        }


        //TODO: Merim, add all other points (feel free to update PointsValue)

        return pointsMap;
    }

    @Transactional
    public void seedFantasyPlayerLeague(Long leagueId) {
        String leagueName = "Fantasy Premijer Liga";

        if(leagueRepository.findByName(leagueName).isPresent()) {
            return;
        }

        FantasyLeague fantasyLeague = new FantasyLeague();
        fantasyLeague.setName(leagueName);

        leagueRepository.findById(leagueId).ifPresent((league -> {
            fantasyLeague.setRegularLeague((RegularLeague) league);
        }));

        leagueRepository.save(fantasyLeague);

        Team fantasyTeam = new Team();
        fantasyTeam.setCode("VUCKO");
        fantasyTeam.setName("Vucko");

        teamRepository.save(fantasyTeam);

        fantasyTeam.getLeagues().add(fantasyLeague);

        teamRepository.save(fantasyTeam);

        for(long playerId = 1L; playerId <= 15; playerId++) {
            Optional<Player> optionalPlayer = playerRepository.findById(playerId);

            if(optionalPlayer.isPresent()) {
                Player player = optionalPlayer.get();

                player.getTeams().add(fantasyTeam);

                playerRepository.save(player);
            }
        }

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
