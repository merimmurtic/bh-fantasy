package com.fifa.wolrdcup.service;

import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
}
