package com.bhfantasy.web.controller;

import com.bhfantasy.web.exception.InvalidLeagueIdException;
import com.bhfantasy.web.exception.InvalidRoundIdException;
import com.bhfantasy.web.exception.InvalidTeamIdException;
import com.bhfantasy.web.exception.LineupNotFoundException;
import com.bhfantasy.web.model.FantasyLineup;
import com.bhfantasy.web.model.Lineup;
import com.bhfantasy.web.model.Round;
import com.bhfantasy.web.model.Team;
import com.bhfantasy.web.model.league.FantasyLeague;
import com.bhfantasy.web.model.league.League;
import com.bhfantasy.web.model.players.*;
import com.bhfantasy.web.repository.*;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import javax.validation.ConstraintViolationException;
import java.util.*;

@RestController
@RequestMapping("/api/leagues/{leagueId}/rounds/{roundId}")
public class LineupController {

    private final LeagueRepository leagueRepository;
    private final LineupRepository lineupRepository;
    private final RoundRepository roundRepository;
    private final TeamRepository teamRepository;
    private final FantasyLineupRepository fantasyLineupRepository;

    public LineupController(LeagueRepository leagueRepository, LineupRepository lineupRepository,
                            RoundRepository roundRepository, TeamRepository teamRepository,
                            FantasyLineupRepository fantasyLineupRepository) {
        this.leagueRepository = leagueRepository;
        this.lineupRepository = lineupRepository;
        this.roundRepository = roundRepository;
        this.teamRepository = teamRepository;
        this.fantasyLineupRepository = fantasyLineupRepository;
    }


    @PostMapping("/teams/{teamId}")
    @JsonView(FantasyLineup.DetailedView.class)
    public ResponseEntity<FantasyLineup> createLineup(
            @RequestBody Lineup lineup,
            @PathVariable("leagueId") Long leagueId,
            @PathVariable("roundId") Long roundId,
            @PathVariable("teamId") Long teamId,
            UriComponentsBuilder builder) throws Exception {

        Optional<League> optionalLeague = leagueRepository.findById(leagueId);

        if (!optionalLeague.isPresent()) {
            throw new InvalidLeagueIdException();
        }

        Optional<Round> optionalRound = roundRepository.findById(roundId);

        if (!optionalRound.isPresent()) {
            throw new InvalidRoundIdException();
        }

        Optional<Team> optionalTeam = teamRepository.findById(teamId);

        if (!optionalTeam.isPresent()) {
            throw new InvalidTeamIdException();
        }

        Team team = optionalTeam.get();

        League league = optionalLeague.get();

        if (league instanceof FantasyLeague) {

            lineup.setId(null);
            validateLineup(lineup);

            Map<Long, Player> playerMap = new HashMap<>();

            for (Player player : team.getPlayers()) {
                playerMap.put(player.getId(), player);
            }

            List<Player> startingPlayers = new ArrayList<>();

            for (Player player : lineup.getStartingPlayers()) {
                if (!playerMap.containsKey(player.getId())) {
                    throwInvalidPlayerIdException(player.getId());
                }

                startingPlayers.add(playerMap.get(player.getId()));
            }

            List<Player> availableSubstitutions = new ArrayList<>();

            for (Player player : lineup.getAvailableSubstitutions()) {
                if (!playerMap.containsKey(player.getId())) {
                    throwInvalidPlayerIdException(player.getId());
                }

                availableSubstitutions.add(playerMap.get(player.getId()));
            }

            lineup.getAvailableSubstitutions().clear();
            lineup.getStartingPlayers().clear();

            if (!playerMap.containsKey(lineup.getCapiten().getId())) {
                throwInvalidPlayerIdException(lineup.getCapiten().getId());
            }

            lineup.setCapiten(playerMap.get(lineup.getCapiten().getId()));

            if (!playerMap.containsKey(lineup.getViceCapiten().getId())) {
                throwInvalidPlayerIdException(lineup.getViceCapiten().getId());
            }

            lineup.setViceCapiten(playerMap.get(lineup.getViceCapiten().getId()));

            try {
                lineup = lineupRepository.save(lineup);
                lineup.getStartingPlayers().addAll(startingPlayers);
                lineup.getAvailableSubstitutions().addAll(availableSubstitutions);

                // Validate again to make sure there is no duplicate player ids in
                // starting players or available substitutions lists
                validateLineup(lineup);

                lineupRepository.save(lineup);

                FantasyLineup fantasyLineup = new FantasyLineup();
                fantasyLineup.setLeague((FantasyLeague) league);
                fantasyLineup.setLineup(lineup);
                fantasyLineup.setRound(optionalRound.get());
                fantasyLineup.setTeam(optionalTeam.get());

                fantasyLineupRepository.save(fantasyLineup);

                return ResponseEntity.created(
                        builder.path("/leagues/{leagueId}/rounds/{roundId}/teams/{teamId}/{id}").
                                buildAndExpand(leagueId, roundId, teamId, fantasyLineup.getId()).toUri()
                ).body(fantasyLineup);
            } catch (ConstraintViolationException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getConstraintViolations().toString());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Regular League is not supported!");
        }
    }

    private void validateLineup(Lineup lineup) {
        if(lineup.getCapiten() == null || lineup.getCapiten().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid Captain is not provided!");
        }

        if(lineup.getViceCapiten() == null || lineup.getViceCapiten().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid Vice-Captain is not provided!");
        }

        if(lineup.getStartingPlayers() == null || lineup.getStartingPlayers().size() != 11) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There should be 11 starting players!");
        }

        if(lineup.getAvailableSubstitutions() == null || lineup.getAvailableSubstitutions().size() != 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There should be 4 available substitutions!");
        }

        if(lineup.getFormation() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid formation needs to be provided!");
        }

        for (Player player : lineup.getStartingPlayers()) {

            if (lineup.getStartingPlayers().size() > 0) {
                List<Player> players = lineup.getPlayersOfType(player.getClass());

                if (player instanceof Goalkeaper) {
                    if (players.size() != 1) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum Goalkeepers!");

                    }
                } else if (player instanceof Defender) {
                    if (players.size() > 4 || players.size() < 2) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum Defenders!");
                    }
                } else if (player instanceof Middle) {
                    if (players.size() > 4 || players.size() < 2) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum Middlers!");
                    }
                } else if (player instanceof Striker) {
                    if (players.size() < 1 || players.size() < 2) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum Strikers!");
                    }
                }
            }
        }
    }

    private void throwInvalidPlayerIdException(Long playerId) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(
                "Invalid player id %s!", playerId));
    }

    @PutMapping("/teams/{teamId}/fantasy-lineups/{fantasyLineupId}/lineups/{lineupId}")
    public Lineup putLineup(@RequestBody Lineup lineup,
                            @PathVariable("leagueId") Long leagueId,
                            @PathVariable("roundId") Long roundId,
                            @PathVariable("teamId") Long teamId,
                            @PathVariable("fantasyLineupId") Long fantasyLineupId,
                            @PathVariable("lineupId") Long lineupId) {

        // Load FantasyLineup by using provided params to make sure it exist
        // FantasyLineup can be loaded by fantasyLineupId only but other params are added to
        // make sure leagueId, roundId, teamId are match with fantasy lineup
        Optional<FantasyLineup> fantasyLineupOptional = fantasyLineupRepository.findByIdAndLeague_IdAndTeam_IdAndRound_Id(
                fantasyLineupId, leagueId, teamId, roundId);

        // Check if fantasy lineup exist and if referenced lineup is one which is provided in request path
        if(!fantasyLineupOptional.isPresent() ||
                fantasyLineupOptional.get().getLineup() == null ||
                !fantasyLineupOptional.get().getLineup().getId().equals(lineupId)) {
            throw new LineupNotFoundException();
        }

        // Validate lineup provided in request to make sure all fields are provided in expected way
        validateLineup(lineup);

        FantasyLineup fantasyLineup = fantasyLineupOptional.get();

        // Populate playerMap with all available players in team
        Map<Long, Player> playerMap = new HashMap<>();

        for (Player player : fantasyLineup.getTeam().getPlayers()) {
            playerMap.put(player.getId(), player);
        }

        Lineup existingLineup = fantasyLineup.getLineup();

        // Populate startingPlayers list with players loaded to Map from database through fantasyLineup.getTeam()
        List<Player> startingPlayers = new ArrayList<>();

        for (Player player : lineup.getStartingPlayers()) {
            if (!playerMap.containsKey(player.getId())) {
                throwInvalidPlayerIdException(player.getId());
            }

            startingPlayers.add(playerMap.get(player.getId()));
        }

        // Populate availableSubstitutions in same way
        List<Player> availableSubstitutions = new ArrayList<>();

        for (Player player : lineup.getAvailableSubstitutions()) {
            if (!playerMap.containsKey(player.getId())) {
                throwInvalidPlayerIdException(player.getId());
            }

            availableSubstitutions.add(playerMap.get(player.getId()));
        }

        // Clear all existing references from availableSubstitutions and startingPlayers
        existingLineup.getAvailableSubstitutions().clear();
        existingLineup.getStartingPlayers().clear();

        // Adding new set of startingPlayers and availableSubstitutions
        existingLineup.getStartingPlayers().addAll(startingPlayers);
        existingLineup.getAvailableSubstitutions().addAll(availableSubstitutions);

        // Set capiten if it's valid
        if (!playerMap.containsKey(lineup.getCapiten().getId())) {
            throwInvalidPlayerIdException(lineup.getCapiten().getId());
        }

        existingLineup.setCapiten(playerMap.get(lineup.getCapiten().getId()));

        // Set vice-capiten if it's valid
        if (!playerMap.containsKey(lineup.getViceCapiten().getId())) {
            throwInvalidPlayerIdException(lineup.getViceCapiten().getId());
        }

        existingLineup.setViceCapiten(playerMap.get(lineup.getViceCapiten().getId()));

        // Set formation
        existingLineup.setFormation(lineup.getFormation());

        // Validate again to make sure there is no duplicate player ids in
        // starting players or available substitutions lists
        validateLineup(existingLineup);

        try {
            return lineupRepository.save(existingLineup);
        } catch (ConstraintViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getConstraintViolations().toString());
        }
    }
}

