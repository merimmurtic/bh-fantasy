package com.fifa.wolrdcup.model.league;

import com.fasterxml.jackson.annotation.JsonView;
import com.fifa.wolrdcup.model.FantasyLineup;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Entity
@DiscriminatorValue("FantasyLeague")
public class FantasyLeague extends League {

    @ManyToMany
    private List<RegularLeague> regularLeagues = new ArrayList<>();

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL)
    @JsonView(LeagueLineupsView.class)
    private List<FantasyLineup> lineups = new LinkedList<>();

    public FantasyLeague(){}

    public List<RegularLeague> getRegularLeagues() {
        return regularLeagues;
    }

    public void setRegularLeagues(List<RegularLeague> regularLeagues) {
        this.regularLeagues = regularLeagues;
    }

    public List<FantasyLineup> getLineups() {
        return lineups;
    }

    public void setLineups(List<FantasyLineup> lineups) {
        this.lineups = lineups;
    }


}
