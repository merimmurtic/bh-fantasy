package com.fifa.wolrdcup.service;

import com.fifa.wolrdcup.model.Match;
import com.fifa.wolrdcup.model.Round;
import com.fifa.wolrdcup.model.Stadium;
import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MatchService {

    private final MatchRepository matchRepository;

    private static Logger logger = LoggerFactory.getLogger(MatchService.class);

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Transactional
    public Match addRound(Long matchId, Round round) {
        Optional<Match> optional = matchRepository.findById(matchId);

        if(optional.isPresent()) {
            optional.get().getRounds().add(round);

            return matchRepository.save(optional.get());
        } else {
            return null;
        }
    }

    @Transactional
    public Optional<Match> getMatch(Long transferMarktId) {
        return matchRepository.findByTransfermarktId(transferMarktId);
    }

    @Transactional
    public Match createMatch(
            Team team1, Team team2, LocalDateTime matchDate,
            Integer score1, Integer score2, Stadium stadium) {
        return createMatch(null, team1, team2, matchDate, score1, score2, stadium);
    }

    @Transactional
    public Match createMatch(
                Long transferMarktId, Team team1, Team team2, LocalDateTime matchDate,
                Integer score1, Integer score2, Stadium stadium) {
        Match match = new Match();
        match.setTransfermarktId(transferMarktId);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setDateTime(matchDate);
        match.setScore1(score1);
        match.setScore2(score2);
        match.setStadium(stadium);

        return matchRepository.save(match);
    }

    @Transactional
    public Match updateMatch(
            Long id, LocalDateTime matchDate, Integer score1, Integer score2) {

        Optional<Match> matchOptional = matchRepository.findById(id);

        if(matchOptional.isPresent()) {
            Match match = matchOptional.get();

            match.setDateTime(matchDate);
            match.setScore1(score1);
            match.setScore2(score2);

            return match;
        } else {
            logger.error("Match with id {} not found.", id);
        }

        return null;
    }

    public MatchRepository getMatchRepository() {
        return matchRepository;
    }
}
