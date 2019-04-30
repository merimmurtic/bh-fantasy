package com.fifa.wolrdcup.model.league;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@DiscriminatorValue("FantasyLeague")
public class FantasyLeague extends League{

    @OneToOne
    private RegularLeague regularLeague;

    public FantasyLeague(){}

    public RegularLeague getRegularLeague() {
        return regularLeague;
    }

    public void setRegularLeague(RegularLeague regularLeague) {
        this.regularLeague = regularLeague;
    }
}
