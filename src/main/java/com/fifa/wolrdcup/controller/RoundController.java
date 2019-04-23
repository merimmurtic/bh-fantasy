package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.model.Round;
import com.fifa.wolrdcup.repository.RoundRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rounds")
public class RoundController {

    private final RoundRepository roundRepository;

    public RoundController(RoundRepository roundRepository){
        this.roundRepository = roundRepository;
    }

    @GetMapping
    public Iterable<Round> getRounds() throws Exception{
        return roundRepository.findAll();
    }

}
