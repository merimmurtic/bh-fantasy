package com.bhfantasy.web.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.bhfantasy.web.model.league.FantasyLeague;

import javax.persistence.*;

@Entity
public class FantasyLineup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonView(FantasyLineupLeagueView.class)
    private FantasyLeague league;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    private Round round;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lineup lineup;

    public FantasyLineup() {}

    public FantasyLeague getLeague() {
        return league;
    }

    public void setLeague(FantasyLeague league) {
        this.league = league;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    public Lineup getLineup() {
        return lineup;
    }

    public void setLineup(Lineup lineup) {
        this.lineup = lineup;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public interface FantasyLineupLeagueView {}

    public interface DetailedView extends FantasyLineupLeagueView {}
}
