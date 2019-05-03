package com.fifa.wolrdcup.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.fifa.wolrdcup.model.league.FantasyLeague;

import javax.persistence.*;

@Entity
public class FantasyLineup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonView(FantasyLineupLeagueView.class)
    private FantasyLeague league;

    @ManyToOne
    private Team team;

    @ManyToOne
    private Round round;

    @ManyToOne
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
