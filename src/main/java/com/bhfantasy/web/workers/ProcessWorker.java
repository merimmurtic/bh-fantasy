package com.bhfantasy.web.workers;

import com.bhfantasy.web.model.Team;
import com.bhfantasy.web.model.league.League;
import com.bhfantasy.web.model.players.Player;
import com.bhfantasy.web.model.players.Unknown;
import com.bhfantasy.web.repository.*;
import com.bhfantasy.web.service.*;

import java.util.Map;

public abstract class ProcessWorker {
    final StadiumRepository stadiumRepository;

    final LeagueService leagueService;

    final RoundService roundService;

    final TeamService teamService;

    final MatchService matchService;

    final PlayerService playerService;

    final GoalRepository goalRepository;

    final LineupRepository lineupRepository;

    final SubstitutionRepository substitutionRepository;

    final CardRepository cardRepository;

    final MissedPenaltyRepository missedPenaltyRepository;

    ProcessWorker(
            StadiumRepository stadiumRepository,
            GoalRepository goalRepository,
            MatchService matchService,
            TeamService teamService,
            RoundService roundService,
            LeagueService leagueService,
            PlayerService playerService,
            LineupRepository lineupRepository,
            SubstitutionRepository substitutionRepository,
            CardRepository cardRepository,
            MissedPenaltyRepository missedPenaltyRepository
    ) {
        this.leagueService = leagueService;
        this.roundService = roundService;
        this.teamService = teamService;
        this.matchService = matchService;
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
        return teamService.processTeam(teamMap.get("code"), teamMap.get("name"), teamMap.get("picture"), league);
    }
}
