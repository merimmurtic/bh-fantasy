package com.bhfantasy.web.controller;

import com.bhfantasy.web.model.Match;
import com.bhfantasy.web.model.Round;
import com.fasterxml.jackson.annotation.JsonView;
import com.bhfantasy.web.model.views.DefaultView;
import com.bhfantasy.web.repository.MatchRepository;
import com.bhfantasy.web.repository.RoundRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leagues/{leagueId}/rounds")
public class LeagueRoundController {

    private final RoundRepository roundRepository;
    private final MatchRepository matchRepository;

    public LeagueRoundController(RoundRepository roundRepository, MatchRepository matchRepository){
        this.roundRepository = roundRepository;
        this.matchRepository = matchRepository;
    }

    @GetMapping
    @JsonView(DefaultView.class)
    public Iterable<Round> getLeagueRounds(@PathVariable("leagueId") Long leagueId) throws Exception{
        return roundRepository.findByLeagueIdOrderById(leagueId);
    }

    @GetMapping("/{roundId}")
    @JsonView(Round.DetailedView.class)
    public ResponseEntity<Round> getRound(
            @PathVariable("leagueId") Long leagueId,
            @PathVariable("roundId") Long roundId) throws Exception{
        return ResponseEntity.of(roundRepository.getByIdAndLeague_Id(roundId, leagueId));
    }

    @GetMapping("/{roundId}/matches/{matchId}")
    @JsonView(Match.DetailedView.class)
    public ResponseEntity<Match> getMatches(
            @PathVariable("leagueId") Long leagueId,
            @PathVariable("roundId") Long roundId,
            @PathVariable("matchId") Long matchId) throws Exception{
        return ResponseEntity.of(
                matchRepository.getDistinctByIdAndRounds_IdAndRounds_League_Id(matchId, roundId, leagueId));
    }


}
