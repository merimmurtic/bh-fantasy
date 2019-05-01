package com.fifa.wolrdcup.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fifa.wolrdcup.model.Match;
import com.fifa.wolrdcup.model.Round;
import com.fifa.wolrdcup.model.views.DefaultView;
import com.fifa.wolrdcup.repository.MatchRepository;
import com.fifa.wolrdcup.repository.RoundRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/leagues/{leagueId}/rounds")
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
        return roundRepository.findByLeagueId(leagueId);
    }

    @GetMapping("/{roundId}")
    @JsonView(Round.DetailedView.class)
    public Iterable<Round> getRounds(
            @PathVariable("leagueId") Long leagueId,
            @PathVariable("roundId") Long roundId) throws Exception{
        return roundRepository.findByIdAndLeague_Id(roundId, leagueId);
    }

    @GetMapping("/{roundId}/matches/{matchId}")
    @JsonView(Match.DetailedView.class)
    public Iterable<Match> getMatches(
            @PathVariable("leagueId") Long leagueId,
            @PathVariable("roundId") Long roundId,
            @PathVariable("matchId") Long matchId) throws Exception{
        return matchRepository.findByIdAndRound_IdAndRound_League_Id(matchId, roundId, leagueId);
    }


}
