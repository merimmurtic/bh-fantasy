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

    @OneToOne
    private RegularLeague regularLeague;

    @OneToMany(mappedBy = "league")
    @JsonView(LeagueLineupsView.class)
    private List<FantasyLineup> lineups = new LinkedList<>();

    public FantasyLeague(){}

    public RegularLeague getRegularLeague() {
        return regularLeague;
    }

    public void setRegularLeague(RegularLeague regularLeague) {
        this.regularLeague = regularLeague;
    }

    public List<FantasyLineup> getLineups() {
        return lineups;
    }

    public void setLineups(List<FantasyLineup> lineups) {
        this.lineups = lineups;
    }


}
