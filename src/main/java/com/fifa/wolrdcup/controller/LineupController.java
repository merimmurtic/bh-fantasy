package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.model.Lineup;
import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.repository.LeagueRepository;
import com.fifa.wolrdcup.repository.LineupRepository;
import com.fifa.wolrdcup.repository.TeamRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import javax.validation.ConstraintViolationException;

@RestController
@RequestMapping("/leagues/{leagueId}/rounds/{roundId}/teams/{teamId}")
public class LineupController {

    private final LeagueRepository leagueRepository;
    private final LineupRepository lineupRepository;
    private final TeamRepository teamRepository;

    public LineupController(LeagueRepository leagueRepository, LineupRepository lineupRepository,
                            TeamRepository teamRepository) {
        this.leagueRepository = leagueRepository;
        this.lineupRepository = lineupRepository;
        this.teamRepository = teamRepository;
    }

    @PostMapping
    public ResponseEntity<Lineup> createFantasyTeam(@RequestBody Lineup lineup, UriComponentsBuilder builder,
                                                    @PathVariable("leagueId") Long leagueId,
                                                    @PathVariable("roundId") Long roundId,
                                                    @PathVariable("teamId")Long teamId) throws Exception {

        Iterable<League> leagues = leagueRepository.findLeagueById(leagueId);
        for (League league : leagues) {
            if (league.getType().equals("FantasyLeague")) {

                lineup.setId(null);

                try {
                    lineup = lineupRepository.save(lineup);

                    return ResponseEntity.created(
                            builder.path("/leagues/{leagueId}/rounds/{roundId}/teams/{teamId}/{id}").
                                    buildAndExpand(lineup.getId()).toUri()
                    ).body(lineup);
                } catch (DataIntegrityViolationException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team with provided code already exist!");
                } catch (ConstraintViolationException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getConstraintViolations().toString());
                }
            }else{
                throw new Exception();
            }

        }
        return null;
    }
}

