package com.fifa.wolrdcup.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fifa.wolrdcup.exception.*;
import com.fifa.wolrdcup.model.*;
import com.fifa.wolrdcup.model.league.FantasyLeague;
import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import javax.validation.ConstraintViolationException;
import java.util.*;

@RestController
@RequestMapping("/leagues/{leagueId}/rounds/{roundId}")
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

            if (lineup.getCapiten() != null && lineup.getCapiten().getId() != null) {
                if (!playerMap.containsKey(lineup.getCapiten().getId())) {
                    throwInvalidPlayerIdException(lineup.getCapiten().getId());
                }

                lineup.setCapiten(playerMap.get(lineup.getCapiten().getId()));
            } else {
                lineup.setCapiten(null);
            }

            if (lineup.getViceCapiten() != null && lineup.getViceCapiten().getId() != null) {
                if (!playerMap.containsKey(lineup.getViceCapiten().getId())) {
                    throwInvalidPlayerIdException(lineup.getViceCapiten().getId());
                }

                lineup.setViceCapiten(playerMap.get(lineup.getViceCapiten().getId()));
            } else {
                lineup.setViceCapiten(null);
            }

            try {
                lineup = lineupRepository.save(lineup);
                lineup.getStartingPlayers().addAll(startingPlayers);
                lineup.getAvailableSubstitutions().addAll(availableSubstitutions);

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

    private void throwInvalidPlayerIdException(Long playerId) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(
                "Invalid player id %s!", playerId));
    }

    @PutMapping("/teams/{teamId}")
    @JsonView(FantasyLineup.DetailedView.class)
    public Lineup putLineup(@RequestBody Lineup lineup,
                            @PathVariable("leagueId") Long leagueId,
                            @PathVariable("roundId") Long roundId,
                            @PathVariable("teamId") Long teamId) {

        if(lineup.getId() == null) {
            throw new LineupNotFoundException();
        }

        Optional<Lineup> existingLineupOptional = lineupRepository.findById(lineup.getId());

        if (existingLineupOptional.isPresent()) {
            Lineup existingLineup = existingLineupOptional.get();

            if (lineup.getStartingPlayers() != null) {
                existingLineup.setStartingPlayers(lineup.getStartingPlayers());
            }

            if (lineup.getAvailableSubstitutions() != null) {
                existingLineup.setAvailableSubstitutions(lineup.getAvailableSubstitutions());
            }

            if (lineup.getCapiten() != null) {
                existingLineup.setCapiten(lineup.getCapiten());
            }

            if (lineup.getViceCapiten() != null) {
                existingLineup.setViceCapiten(lineup.getViceCapiten());
            }

            if (lineup.getFormation() != null) {
                existingLineup.setFormation(lineup.getFormation());
            }

            if (lineup.getSubstitutionChanges() != null) {
                existingLineup.setSubstitutionChanges(lineup.getSubstitutionChanges());
            }

            return lineupRepository.save(existingLineup);
        }
        return lineup;
    }
}

