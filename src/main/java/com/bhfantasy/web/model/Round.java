package com.bhfantasy.web.model;

import com.bhfantasy.web.model.league.League;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@NamedEntityGraph(name = "Round.detail",
        attributeNodes = {
            @NamedAttributeNode(value = "matches", subgraph = "matches-subgraph"),
            @NamedAttributeNode("league")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "matches-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("team1"),
                                @NamedAttributeNode("team2"),
                                @NamedAttributeNode("stadium")
                        }
                )
        })
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "rounds")
    @JsonView(RoundMatchesView.class)
    @OrderBy("id")
    private Set<Match> matches = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonView(RoundLeagueView.class)
    private League league;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    public Round(){}

    public Round(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Match> getMatches() {
        return matches;
    }

    public void setMatches(Set<Match> matches) {
        this.matches = matches;
    }

    public League getLeague() {
        return league;
    }

    public void setLeague(League league) {
        this.league = league;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public interface RoundMatchesView {}

    public interface RoundLeagueView {}

    public interface DetailedView extends RoundMatchesView, RoundLeagueView {}
}
