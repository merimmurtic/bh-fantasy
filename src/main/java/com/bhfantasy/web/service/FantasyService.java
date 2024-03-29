package com.bhfantasy.web.service;

import com.bhfantasy.web.model.custom.PointsValue;
import com.bhfantasy.web.model.players.Goalkeaper;
import com.bhfantasy.web.exception.InvalidLeagueIdException;
import com.bhfantasy.web.model.league.FantasyLeague;
import com.bhfantasy.web.model.league.RegularLeague;
import com.bhfantasy.web.model.players.Defender;
import com.bhfantasy.web.model.players.Middle;
import com.bhfantasy.web.model.players.Player;
import com.bhfantasy.web.model.*;
import com.bhfantasy.web.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class FantasyService {

    private static Logger logger = LoggerFactory.getLogger(FantasyService.class);

    private final RegularLeagueRepository regularLeagueRepository;

    private final FantasyLeagueRepository fantasyLeagueRepository;

    private final PlayerPointsRepository playerPointsRepository;

    private final MatchRepository matchRepository;

    public FantasyService(PlayerPointsRepository playerPointsRepository,
                          RegularLeagueRepository regularLeagueRepository,
                          FantasyLeagueRepository fantasyLeagueRepository,
                          MatchRepository matchRepository) {
        this.regularLeagueRepository = regularLeagueRepository;
        this.fantasyLeagueRepository = fantasyLeagueRepository;
        this.playerPointsRepository = playerPointsRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional
    public void process(Long leagueId) {
        Optional<RegularLeague> leagueOptional = regularLeagueRepository.findById(leagueId);

        if(!leagueOptional.isPresent()) {
            throw new InvalidLeagueIdException();
        }

        RegularLeague league = leagueOptional.get();

        for(Round round : league.getRounds()) {
            for(Match match : round.getMatches()) {
                processMatch(match);
            }
        }
    }

    @Transactional
    public void processMatch(Match match) {
        if(match.getScore1() == null || match.getPlayerPoints().size() > 0) {
            return;
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

    private Map<Long, PointsValue> calculatePointsForMatch(Match match) {
        Map<Long, PointsValue> pointsMap = new HashMap<>();

        for(Goal goal : match.getGoals()) {
            if(goal.getPlayer() != null) {
                pointsMap.putIfAbsent(goal.getPlayer().getId(), new PointsValue(goal.getPlayer()));

                if (!goal.getOwnGoal()) {
                    pointsMap.get(goal.getPlayer().getId()).addGoal();
                } else {
                    pointsMap.get(goal.getPlayer().getId()).addOwnGoal();
                }
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

        for(MissedPenalty missedPenalty : match.getMissedPenalties()){
            if(missedPenalty.getSavedBy() != null) {
                pointsMap.putIfAbsent(missedPenalty.getSavedBy().getId(), new PointsValue(missedPenalty.getSavedBy()));

                pointsMap.get(missedPenalty.getSavedBy().getId()).addSavedPenalty();
            }

            if(missedPenalty.getPlayer() != null){
                pointsMap.putIfAbsent(missedPenalty.getPlayer().getId(), new PointsValue(missedPenalty.getPlayer()));

                pointsMap.get(missedPenalty.getPlayer().getId()).addMissedPenalty();
            }
        }

        Map<Long, Integer> minutesPlayedLineup1 = getPlayerMinutes(match.getLineup1(), pointsMap, match);
        Map<Long, Integer> minutesPlayedLineup2 = getPlayerMinutes(match.getLineup2(), pointsMap, match);

        // TODO: There is a bug here, we need to check if goal is scored while player is on pitch
        minutesPlayedLineup1.forEach((playerId, minutes) -> {
            Player player = pointsMap.get(playerId).getPlayer();

            if(minutes >= 60 && match.getScore2() == 0 && player
                    instanceof Defender || player instanceof Goalkeaper || player instanceof Middle) {
                pointsMap.get(playerId).addCleanSheet();
            }
        });

        minutesPlayedLineup2.forEach((playerId, minutes) -> {
            Player player = pointsMap.get(playerId).getPlayer();

            if(minutes >= 60 && match.getScore1() == 0 && player
                    instanceof Defender || player instanceof Goalkeaper || player instanceof Middle) {
                pointsMap.get(playerId).addCleanSheet();
            }
        });

        Map<Long, Integer> minutesPlayed = new HashMap<>();
        minutesPlayed.putAll(minutesPlayedLineup1);
        minutesPlayed.putAll(minutesPlayedLineup2);

        for(Long playerId: minutesPlayed.keySet()) {
            pointsMap.get(playerId).addMinutesPlayed(minutesPlayed.get(playerId));
        }

        return pointsMap;
    }

    private Map<Long, Integer> getPlayerMinutes(Lineup lineup, Map<Long, PointsValue> pointsMap, Match match) {
        Map<Long, Integer> result = new HashMap<>();

        if(lineup == null) {
            return result;
        }

        for(Player player: lineup.getStartingPlayers()) {
            pointsMap.putIfAbsent(player.getId(), new PointsValue(player));

            result.put(player.getId(), 90);
        }

        for(Substitution substitution: lineup.getSubstitutionChanges()) {
            if(substitution.getPlayer() != null) {
                pointsMap.putIfAbsent(substitution.getPlayer().getId(), new PointsValue(substitution.getPlayer()));

                result.put(substitution.getPlayer().getId(), 90 - substitution.getMinute());
            }
        }

        for (Substitution substitution : lineup.getSubstitutionChanges()) {
            Integer minutesPlayed = result.get(substitution.getSubstitutePlayer().getId());

            if(minutesPlayed == null) {
                match.setReviewRequired(true);

                matchRepository.save(match);

                logger.error(
                        "Error while processing minutes for player {}", substitution.getSubstitutePlayer().getId());
                continue;
            }

            result.put(substitution.getSubstitutePlayer().getId(), minutesPlayed - (90 - substitution.getMinute()));
        }

        return result;
    }

    @Transactional
    public FantasyLeague createFantasyPlayerLeague(Long leagueId) {
        Optional<RegularLeague> regularLeagueOptional = regularLeagueRepository.findById(leagueId);

        if (!regularLeagueOptional.isPresent()) {
            logger.warn("Regular league doesn't exist, skip seeding Fantasy League");
            return null;
        }

        RegularLeague regularLeague = regularLeagueOptional.get();

        String leagueName = "Fantasy " + regularLeague.getName();

        Optional<FantasyLeague> fantasyLeagueOptional = fantasyLeagueRepository.findByRegularLeague_Id(
                regularLeague.getId());

        if(fantasyLeagueOptional.isPresent()) {
            logger.info(leagueName.concat(" is already seeded!"));
            return fantasyLeagueOptional.get();
        }

        FantasyLeague fantasyLeague = new FantasyLeague();
        fantasyLeague.setName(leagueName);
        fantasyLeague.setRegularLeague(regularLeague);

        fantasyLeagueRepository.save(fantasyLeague);

        return fantasyLeague;
    }
}
