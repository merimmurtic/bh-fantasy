package com.fifa.wolrdcup.service;

import com.fifa.wolrdcup.exception.InvalidLeagueIdException;
import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.model.league.RegularLeague;
import com.fifa.wolrdcup.repository.RegularLeagueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class LeagueService {

    private final RegularLeagueRepository regularLeagueRepository;

    private static Logger logger = LoggerFactory.getLogger(LeagueService.class);

    public LeagueService(RegularLeagueRepository regularLeagueRepository) {
        this.regularLeagueRepository = regularLeagueRepository;
    }

    @Transactional
    public RegularLeague processRegularLeague(String leagueName, String season) {
        Optional<RegularLeague> optionalLeague = regularLeagueRepository.findByNameAndSeason(leagueName, season);

        RegularLeague league = null;

        if(optionalLeague.isPresent()) {
            league = optionalLeague.get();
        } else {
            league = new RegularLeague();
            league.setName(leagueName);
            league.setSeason(season);

            regularLeagueRepository.save(league);
        }

        return league;
    }

    @Transactional
    public RegularLeague getLeagueGroup(Long leagueId, String groupName) {
        Optional<RegularLeague> regularLeague = regularLeagueRepository.findGroup(leagueId, groupName);

        return regularLeague.orElse(null);
    }

    @Transactional
    public RegularLeague addGroup(Long leagueId, RegularLeague group) {
        Optional<RegularLeague> optional = regularLeagueRepository.findById(leagueId);

        if(optional.isPresent()) {
            optional.get().getGroups().add(group);

            return regularLeagueRepository.save(optional.get());
        } else {
            throw new InvalidLeagueIdException();
        }
    }

    public RegularLeagueRepository getRegularLeagueRepository() {
        return regularLeagueRepository;
    }
}
