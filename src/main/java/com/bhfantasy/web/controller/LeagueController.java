package com.bhfantasy.web.controller;

import com.bhfantasy.web.exception.InvalidLeagueIdException;
import com.bhfantasy.web.model.Round;
import com.bhfantasy.web.model.Team;
import com.bhfantasy.web.model.custom.TransferInfoValue;
import com.bhfantasy.web.model.league.FantasyLeague;
import com.bhfantasy.web.model.league.League;
import com.bhfantasy.web.model.league.LeagueGroup;
import com.bhfantasy.web.model.league.RegularLeague;
import com.bhfantasy.web.repository.*;
import com.bhfantasy.web.service.LeagueService;
import com.fasterxml.jackson.annotation.JsonView;
import com.bhfantasy.web.exception.InvalidTeamIdException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.ConstraintViolationException;
import java.util.Optional;

@RestController
@RequestMapping("/api/leagues")
public class LeagueController {

    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final RoundRepository roundRepository;
    private final LeagueGroupRepository leagueGroupRepository;
    private final RegularLeagueRepository regularLeagueRepository;
    private final LeagueService leagueService;

    public LeagueController(
            TeamRepository teamRepository,
            LeagueRepository leagueRepository,
            RoundRepository roundRepository, LeagueGroupRepository leagueGroupRepository, RegularLeagueRepository regularLeagueRepository, LeagueService leagueService) {
        this.leagueRepository = leagueRepository;
        this.teamRepository = teamRepository;
        this.roundRepository = roundRepository;
        this.leagueGroupRepository = leagueGroupRepository;
        this.regularLeagueRepository = regularLeagueRepository;
        this.leagueService = leagueService;
    }

    @GetMapping
    @JsonView(League.LeagueGroupsView.class)
    public Iterable<RegularLeague> getLeagues() throws Exception{
        return regularLeagueRepository.getAllLeagues();
    }

    @PostMapping
    @JsonView(League.DetailedView.class)
    public ResponseEntity<League> createLeague(@RequestBody League league, UriComponentsBuilder builder) {
        // Make sure id is null to avoid update of existing league
        league.setId(null);

        try {
            if(league instanceof FantasyLeague) {
                FantasyLeague fantasyLeague = (FantasyLeague) league;

                Long regularLeagueId = fantasyLeague.getRegularLeague().getId();

                Optional<League> regularLeagueOptional = leagueRepository.getById(regularLeagueId);

                if(!regularLeagueOptional.isPresent()) {
                    throw new InvalidLeagueIdException();
                }

                League regularLeague = regularLeagueOptional.get();

                if(regularLeague instanceof RegularLeague) {
                    fantasyLeague.setRegularLeague((RegularLeague) regularLeague);
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided league is not Regular League!");
                }
            }

            league = leagueRepository.save(league);

            //TODO: This is the right way to handle creation of model, try to do same for player and team creation
            return ResponseEntity.created(
                    builder.path("/leagues/{id}").buildAndExpand(league.getId()).toUri()
            ).body(league);
        } catch (DataIntegrityViolationException e) {
            // DataIntegrityViolationException is thrown in case unique constraint fails!
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "league with provided name already exist!");
        } catch (ConstraintViolationException e) {
            // In case validation of model fail ConstraintViolationException is thrown (for example, name is null)
            // in this case throw ResponseStatusException with details about contstraint violations
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getConstraintViolations().toString());
        }
    }

    @PostMapping("/{leagueId}/add-team")
    public void referenceTeam(
            @PathVariable("leagueId") Long leagueId,
            @RequestBody Team team) {
        // TODO: Do implementation here, inside team object expect id only!
        // Load league for provided leagueId, update with it provided team and save it
        // Make sure to handle validation it provided leagueId doesn't exist, or provided team id doesn't exist
        if(team.getId() != null){
            Optional<Team> existingTeamOptional = teamRepository.findById(team.getId());

            if(!existingTeamOptional.isPresent()) {
                throw new InvalidTeamIdException();
            }

            Optional<League> existingLeagueOptional = leagueRepository.findById(leagueId);

            if(!existingLeagueOptional.isPresent()) {
                throw new InvalidLeagueIdException();
            }

            League league = existingLeagueOptional.get();

            team = existingTeamOptional.get();

            if(!team.getLeagues().contains(league)) {
                team.getLeagues().add(league);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Team is already referenced with provided league!");
            }

            teamRepository.save(team);
        } else {
            throw new InvalidTeamIdException();
        }
    }


    @GetMapping("/{leagueId}")
    @JsonView(value = {League.DetailedView.class})
    public ResponseEntity<League> getLeague(@PathVariable("leagueId") Long leagueId) throws Exception {
        Optional<League> optionalLeague = leagueRepository.getById(leagueId);

        optionalLeague.ifPresent(league -> {
            if(league instanceof LeagueGroup) {
                LeagueGroup regularLeague = (LeagueGroup)league;

                Optional<Round> optionalRound = roundRepository.
                        findFirstByLeagueIdAndMatches_Score1IsNotNullOrderByMatches_DateTimeDesc(leagueId);

                optionalRound.ifPresent(round -> {
                    league.setCurrentRoundId(round.getId());
                });

                if(league instanceof RegularLeague) {
                    ((RegularLeague)regularLeague).setGroups(leagueGroupRepository.getGroupsWithTeams(leagueId));
                }
            }
        });

        return ResponseEntity.of(optionalLeague);
    }

    @PostMapping("/{leagueId}/{teamId}/transfers")
    public ResponseEntity<Team> makeTransfer(
            @PathVariable("leagueId") Long leagueId,
            @PathVariable("teamId") Long teamId,
            @RequestBody TransferInfoValue transferInfoValue) {

        Team team = leagueService.makeTransfers(leagueId, teamId, transferInfoValue);

        return ResponseEntity.ok(team);
    }
}
