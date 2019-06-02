package com.bhfantasy.web.model.league;

import com.fasterxml.jackson.annotation.JsonView;
import com.bhfantasy.web.model.Round;

import javax.persistence.*;
import java.util.*;

@Entity
@DiscriminatorValue("LeagueGroup")
public class LeagueGroup extends League{

    @OneToMany(mappedBy = "league")
    @JsonView(LeagueRoundsView.class)
    @OrderBy("id")
    private Set<Round> rounds = new HashSet<>();

    public LeagueGroup(){}

    public Set<Round> getRounds() {
        return rounds;
    }

    public void setRounds(Set<Round> rounds) {
        this.rounds = rounds;
    }
}
