package com.fifa.wolrdcup.service;

import com.fifa.wolrdcup.model.Match;
import com.fifa.wolrdcup.model.Round;
import com.fifa.wolrdcup.model.league.RegularLeague;
import com.fifa.wolrdcup.repository.MatchRepository;
import com.fifa.wolrdcup.repository.RoundRepository;
import com.fifa.wolrdcup.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MultiLeagueService {

    private static Logger logger = LoggerFactory.getLogger(MultiLeagueService.class);

    private final MatchRepository matchRepository;

    private final RoundRepository roundRepository;

    public MultiLeagueService(MatchRepository matchRepository, RoundRepository roundRepository) {
        this.matchRepository = matchRepository;
        this.roundRepository = roundRepository;
    }

    @Transactional
    public void calculateRounds(RegularLeague league) {
        List<Round> rounds = new ArrayList<>();

        List<Match> matches = matchRepository.findMultiLeagueMatches(league.getId());

        LocalDateTime roundStart = null;

        int counter = 1;

        Round round = null;

        for (Match match : matches) {
            if(!CommonUtils.checkIfSameWeek(roundStart, match.getDateTime())) {
                if(round != null) {
                    roundRepository.save(round);
                }

                round = new Round();
                round.setStartDate(match.getDateTime());
                round.setName(counter + ".Matchday");
                round.setLeague(league);

                round = roundRepository.save(round);

                rounds.add(round);

                counter += 1;
            } else {
                round.setEndDate(match.getDateTime());
            }

            match.getRounds().add(round);

            roundStart = match.getDateTime();
        }

        if (round != null) {
            roundRepository.save(round);
        }

        logger.info("{} rounds calculated for league {}!", rounds.size(), league.getName());
    }
}
