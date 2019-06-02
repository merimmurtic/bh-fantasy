package com.bhfantasy.web.service;

import com.bhfantasy.web.exception.InvalidLeagueIdException;
import com.bhfantasy.web.exception.InvalidPlayerIdException;
import com.bhfantasy.web.exception.InvalidTeamIdException;
import com.bhfantasy.web.model.Team;
import com.bhfantasy.web.model.custom.TransferInfoValue;
import com.bhfantasy.web.model.league.LeagueGroup;
import com.bhfantasy.web.model.league.RegularLeague;
import com.bhfantasy.web.model.players.Player;
import com.bhfantasy.web.repository.LeagueGroupRepository;
import com.bhfantasy.web.repository.PlayerRepository;
import com.bhfantasy.web.repository.RegularLeagueRepository;
import com.bhfantasy.web.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class LeagueService {

    private final RegularLeagueRepository regularLeagueRepository;

    private final LeagueGroupRepository leagueGroupRepository;

    private final TeamRepository teamRepository;

    private final PlayerRepository playerRepository;

    private static Logger logger = LoggerFactory.getLogger(LeagueService.class);

    public LeagueService(RegularLeagueRepository regularLeagueRepository,
                         LeagueGroupRepository leagueGroupRepository,
                         TeamRepository teamRepository, PlayerRepository playerRepository) {
        this.regularLeagueRepository = regularLeagueRepository;
        this.leagueGroupRepository = leagueGroupRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
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
    public LeagueGroup getLeagueGroup(Long leagueId, String groupName) {
        Optional<LeagueGroup> regularLeague = leagueGroupRepository.findGroup(leagueId, groupName);

        return regularLeague.orElse(null);
    }

    @Transactional
    public LeagueGroup createLeagueGroup(String groupName) {
        LeagueGroup group = new LeagueGroup();
        group.setName(groupName);

        return leagueGroupRepository.save(group);
    }

    @Transactional
    public RegularLeague createRegularLeague(String leagueName, String season) {
        RegularLeague league = new RegularLeague();
        league.setName(leagueName);
        league.setSeason(season);

        return regularLeagueRepository.save(league);
    }

    @Transactional
    public Optional<RegularLeague> getRegularLeague(String leagueName, String season) {
        return regularLeagueRepository.findByNameAndSeason(leagueName, season);
    }

    @Transactional
    public RegularLeague addGroup(Long leagueId, LeagueGroup group) {
        Optional<RegularLeague> optional = regularLeagueRepository.findById(leagueId);

        if(optional.isPresent()) {
            optional.get().getGroups().add(group);

            return regularLeagueRepository.save(optional.get());
        } else {
            throw new InvalidLeagueIdException();
        }
    }

    @Transactional
    public Team makeTransfers(Long leagueId, Long teamId, TransferInfoValue transferInfoValue){
        Optional<Team> optionalTeam = teamRepository.getFantasyTeam(teamId, leagueId);

        if(!optionalTeam.isPresent()){
            throw new InvalidTeamIdException();
        }

        Team team = optionalTeam.get();

        if (transferInfoValue.getTransferIn().size() != transferInfoValue.getTransferOut().size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You must to have the same number of players on transferIn and transferOut");
        }

        Map<Long, Player> playersMap = team.getPlayers().stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.getId(), item), Map::putAll);

        for(Player player : transferInfoValue.getTransferOut()) {
            Player dbPlayer = playersMap.get(player.getId());

            if(dbPlayer == null) {
                throw new InvalidPlayerIdException();
            }

            team.getPlayers().remove(dbPlayer);
        }

        for(Player player : transferInfoValue.getTransferIn()) {
            Optional<Player> optionalPlayer = playerRepository.findByIdAndTeams_Leagues_Id(player.getId(), leagueId);

            if(!optionalPlayer.isPresent()) {
                throw new InvalidPlayerIdException();
            }

            team.getPlayers().add(optionalPlayer.get());
        }

        return team;
    }

    public RegularLeagueRepository getRegularLeagueRepository() {
        return regularLeagueRepository;
    }
}
