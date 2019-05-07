package com.fifa.wolrdcup.workers;

import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.model.players.Unknown;
import com.fifa.wolrdcup.repository.*;
import com.fifa.wolrdcup.service.PlayerService;

import java.util.Map;
import java.util.Optional;

public abstract class ProcessWorker {
    final StadiumRepository stadiumRepository;

    final LeagueRepository leagueRepository;

    final RoundRepository roundRepository;

    final TeamRepository teamRepository;

    final MatchRepository matchRepository;

    final PlayerService playerService;

    final GoalRepository goalRepository;

    final LineupRepository lineupRepository;

    final SubstitutionRepository substitutionRepository;

    final CardRepository cardRepository;

    final MissedPenaltyRepository missedPenaltyRepository;

    ProcessWorker(
            StadiumRepository stadiumRepository,
            GoalRepository goalRepository,
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            RoundRepository roundRepository,
            LeagueRepository leagueRepository,
            PlayerService playerService,
            LineupRepository lineupRepository,
            SubstitutionRepository substitutionRepository,
            CardRepository cardRepository,
            MissedPenaltyRepository missedPenaltyRepository
    ) {
        this.leagueRepository = leagueRepository;
        this.roundRepository = roundRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.playerService = playerService;
        this.goalRepository = goalRepository;
        this.stadiumRepository = stadiumRepository;
        this.lineupRepository = lineupRepository;
        this.substitutionRepository = substitutionRepository;
        this.cardRepository = cardRepository;
        this.missedPenaltyRepository = missedPenaltyRepository;
    }

    public abstract Long process() throws Exception;

    Player processPlayer(String firstName, String lastName, Team team) {
        return processPlayer(firstName, lastName, team, null);
    }

    Player processPlayer(String firstName, String lastName, Team team, Long transferMarktId) {
        Player player = new Unknown();
        player.setFirstName(firstName);
        player.setLastName(lastName);
        player.setTransferMarktId(transferMarktId);

        return playerService.processPlayer(player, team);
    }

    Team processTeam(Map<String, String> teamMap, League league) {
        Team team = new Team();
        team.setCode(teamMap.get("code"));
        team.setName(teamMap.get("name"));
        team.setProfilePicture(teamMap.get("picture"));
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
