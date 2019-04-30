package com.fifa.wolrdcup.model.league;

import com.fasterxml.jackson.annotation.JsonView;
import com.fifa.wolrdcup.model.Round;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.LinkedList;
import java.util.List;

@Entity
@DiscriminatorValue("RegularLeague")
public class RegularLeague extends League{

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL)
    @JsonView(LeagueRoundsView.class)
    private List<Round> rounds = new LinkedList<>();

    public RegularLeague(){}

    public List<Round> getRounds() {
        return rounds;
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }
}
