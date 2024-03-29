package com.bhfantasy.web.service;

import com.bhfantasy.web.model.Round;
import com.bhfantasy.web.model.league.League;
import com.bhfantasy.web.repository.RoundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class RoundService {

    private final RoundRepository roundRepository;

    private static Logger logger = LoggerFactory.getLogger(RoundService.class);

    public RoundService(RoundRepository roundRepository) {
        this.roundRepository = roundRepository;
    }

    @Transactional
    public Round getOrCreateRound(String roundName, League league) {
        Optional<Round> optionalRound = roundRepository.findByLeagueIdAndName(league.getId(), roundName);

        Round round;

        if(!optionalRound.isPresent()) {
            round = new Round();
            round.setName(roundName);
            round.setLeague(league);

            round = roundRepository.save(round);
        } else {
            round = optionalRound.get();
            round.getMatches().clear();
        }

        return round;
    }

    public RoundRepository getRoundRepository() {
        return roundRepository;
    }
}
