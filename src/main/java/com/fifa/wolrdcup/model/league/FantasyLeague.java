package com.fifa.wolrdcup.model.league;

import com.fasterxml.jackson.annotation.JsonView;
import com.fifa.wolrdcup.model.FantasyLineup;

import javax.persistence.*;
import java.util.*;

@Entity
@DiscriminatorValue("FantasyLeague")
public class FantasyLeague extends League {

    @OneToOne
    private RegularLeague regularLeague;

    @OneToMany(mappedBy = "league")
    @JsonView(LeagueLineupsView.class)
    @OrderBy("id")
    private Set<FantasyLineup> lineups = new HashSet<>();

    public FantasyLeague(){}

    public RegularLeague getRegularLeague() {
        return regularLeague;
    }

    public void setRegularLeague(RegularLeague regularLeague) {
        this.regularLeague = regularLeague;
    }

    public Set<FantasyLineup> getLineups() {
        return lineups;
    }

    public void setLineups(Set<FantasyLineup> lineups) {
        this.lineups = lineups;
    }


}
