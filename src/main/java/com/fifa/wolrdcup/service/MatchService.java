package com.fifa.wolrdcup.service;

import com.fifa.wolrdcup.model.Match;
import com.fifa.wolrdcup.model.Round;
import com.fifa.wolrdcup.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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

    public MatchRepository getMatchRepository() {
        return matchRepository;
    }
}
