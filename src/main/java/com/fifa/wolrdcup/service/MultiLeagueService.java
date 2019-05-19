package com.fifa.wolrdcup.service;

import com.fifa.wolrdcup.model.Match;
import com.fifa.wolrdcup.model.Round;
import com.fifa.wolrdcup.model.league.RegularLeague;
import com.fifa.wolrdcup.repository.MatchRepository;
import com.fifa.wolrdcup.repository.RoundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
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
            if(!checkIfSameWeek(roundStart, match.getDateTime())) {
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

    private boolean checkIfSameWeek(LocalDateTime localDateTime1, LocalDateTime localDateTime2) {
        if(localDateTime1 == null || localDateTime2 == null) {
            return false;
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(localDateTime1.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        int week = cal1.get(Calendar.WEEK_OF_YEAR);
        int year = cal1.get(Calendar.YEAR);

        Calendar cal2 = Calendar.getInstance();

        cal2.setTimeInMillis(localDateTime2.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        int targetWeek = cal2.get(Calendar.WEEK_OF_YEAR);
        int targetYear = cal2.get(Calendar.YEAR);

        return week == targetWeek && year == targetYear;
    }
}
