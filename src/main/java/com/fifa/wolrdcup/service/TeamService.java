package com.fifa.wolrdcup.service;

import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.model.custom.TransferInfoValue;
import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.swing.text.html.Option;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    private static Logger logger = LoggerFactory.getLogger(TeamService.class);

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Transactional
    public Team processTeam(String code, String name, String picture, League league) {
        Team team = new Team();
        team.setCode(code);
        team.setName(name);
        team.setProfilePicture(picture);

        Optional<Team> existingTeamOptional = teamRepository.findByCode(team.getCode());

        if(existingTeamOptional.isPresent()) {
            Team existingTeam = existingTeamOptional.get();

            boolean update = false;

            if(team.getName() != null && !team.getName().equals(existingTeam.getName())) {
                existingTeam.setName(team.getName());

                update = true;
            }

            if(team.getCode() != null && !team.getCode().equals(existingTeam.getCode())) {
                existingTeam.setCode(team.getCode());

                update = true;
            }

            if(team.getProfilePicture() != null && !team.getProfilePicture().equals(existingTeam.getProfilePicture())) {
                existingTeam.setProfilePicture(team.getProfilePicture());

                update = true;
            }

            if(update) {
                team = teamRepository.save(existingTeam);
            } else {
                team = existingTeam;
            }

            Set<League> leagues = team.getLeagues();

            if(leagues.stream().noneMatch(l -> l.getId().equals(league.getId()))) {
                leagues.add(league);
            }

            return team;
        } else {
            team = teamRepository.save(team);

            team.getLeagues().add(league);
        }

        return team;
    }

    public TeamRepository getTeamRepository() {
        return teamRepository;
    }

    @Transactional
    public Team makeTransfer(Long teamId, TransferInfoValue transferInfoValue){

        Optional<Team> optionalTeam = teamRepository.getFantasyTeam(teamId);

        if(!optionalTeam.isPresent()){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This team is not present");
        }

        Team team = optionalTeam.get();

        if (transferInfoValue.getTransferIn().size() != transferInfoValue.getTransferOut().size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You must to have the same number of players on transferIn and transferOut");
        }

        //logika :D

        return team;
    }
}
