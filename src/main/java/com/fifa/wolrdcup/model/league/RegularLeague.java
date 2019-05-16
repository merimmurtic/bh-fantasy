package com.fifa.wolrdcup.model.league;

import com.fasterxml.jackson.annotation.JsonView;
import com.fifa.wolrdcup.model.Round;
import com.fifa.wolrdcup.model.Team;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Entity
@DiscriminatorValue("RegularLeague")
public class RegularLeague extends League{

    private String season;

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL)
    @JsonView(LeagueRoundsView.class)
    private List<Round> rounds = new LinkedList<>();

    @ManyToMany
    private List<RegularLeague> groups = new ArrayList<>();

    public RegularLeague(){}

    public List<Round> getRounds() {
        return rounds;
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public List<RegularLeague> getGroups() {
        return groups;
    }

    public void setGroups(List<RegularLeague> groups) {
        this.groups = groups;
    }

    @Override
    public List<Team> getTeams() {
        List<Team> teams = new ArrayList<>(super.getTeams());

        groups.forEach(group -> {
            teams.addAll(group.getTeams());
        });

        return teams;
    }
}
