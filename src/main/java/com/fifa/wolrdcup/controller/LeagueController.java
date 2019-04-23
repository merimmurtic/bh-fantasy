package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.exception.InvalidLeagueIdException;
import com.fifa.wolrdcup.model.League;
import com.fifa.wolrdcup.repository.LeagueRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/leagues")
public class LeagueController {

    private final LeagueRepository leagueRepository;

    public LeagueController(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @GetMapping
    public Iterable<League> getLeagues() throws Exception{
        return leagueRepository.findAll();
    }

    @PostMapping
    public void createLeague(@RequestBody League league) throws Exception {
        if(league.getId() != null){
            Optional<League> existingLeagueOptional = leagueRepository.findById(league.getId());
            if(existingLeagueOptional.isPresent()){
                throw new InvalidLeagueIdException();
            } else{
                leagueRepository.save(league);
            }
        }
    }
    @GetMapping("/{leagueId}")
    public ResponseEntity<League> getLeague(@PathVariable("leagueId") Long leagueId) throws Exception {
        return ResponseEntity.of(leagueRepository.findById(leagueId));
    }


}
