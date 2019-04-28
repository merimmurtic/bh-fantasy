package com.fifa.wolrdcup.workers;

import com.fifa.wolrdcup.model.League;
import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.model.players.Unknown;
import com.fifa.wolrdcup.repository.*;

import java.util.Map;
import java.util.Optional;

abstract class ProcessWorker {
    final StadiumRepository stadiumRepository;

    final LeagueRepository leagueRepository;

    final RoundRepository roundRepository;

    final TeamRepository teamRepository;

    final MatchRepository matchRepository;

    final PlayerRepository playerRepository;

    final GoalRepository goalRepository;

    ProcessWorker(
            StadiumRepository stadiumRepository,
            GoalRepository goalRepository,
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            RoundRepository roundRepository,
            LeagueRepository leagueRepository,
            PlayerRepository playerRepository) {
        this.leagueRepository = leagueRepository;
        this.roundRepository = roundRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.goalRepository = goalRepository;
        this.stadiumRepository = stadiumRepository;
    }

    abstract void process() throws Exception;

    Player processPlayer(String firstName, String lastName, Team team) {
        return processPlayer(firstName, lastName, team, null);
    }

    Player processPlayer(String firstName, String lastName, Team team, Long transferMarktId) {
        Player player = new Unknown();
        player.setLastName(lastName);
        player.setFirstName(firstName);
        player.setTeam(team);
        player.setTransferMarktId(transferMarktId);

        Optional<Player> existingPlayer = Optional.empty();

        if(transferMarktId != null) {
            existingPlayer = playerRepository.findByTransferMarktId(transferMarktId);
        }

        if(!existingPlayer.isPresent() && firstName != null) {
            existingPlayer = playerRepository.findByTeamAndFirstNameAndLastName(
                    team, firstName, lastName);
        }

        if(!existingPlayer.isPresent()) {
            existingPlayer = playerRepository.findByTeamAndLastName(
                    team, lastName);
        }

        existingPlayer.ifPresent((p) -> {
            boolean updated = false;

            if(firstName != null && p.getFirstName() == null) {
                p.setFirstName(firstName);
                updated = true;
            }

            if(transferMarktId != null && p.getTransferMarktId() == null) {
                p.setTransferMarktId(transferMarktId);
                updated = true;
            }

            if(team != null && !team.getId().equals(p.getTeamId())) {
                p.setTeam(team);
                updated = true;
            }

            if(updated) {
                playerRepository.save(p);
            }
        });

        return existingPlayer.orElseGet(() -> playerRepository.save(player));
    }

    Team processTeam(Map<String, String> teamMap, League league) {
        Team team = new Team();
        team.setCode(teamMap.get("code"));
        team.setName(teamMap.get("name"));
        team.getLeagues().add(league);

        Optional<Team> existingTeam = teamRepository.findByCodeAndLeagues(team.getCode(), league);

        //if(!existingTeam.isPresent()) {
        //  return teamRepository.save(team);
        //} else {
        //  return existingTeam.get();
        //}
        return existingTeam.orElseGet(() -> teamRepository.save(team));

    }
}
