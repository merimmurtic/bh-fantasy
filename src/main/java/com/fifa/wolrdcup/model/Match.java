package com.fifa.wolrdcup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NamedEntityGraph(name = "Match.detail",
        attributeNodes = {
                @NamedAttributeNode("team1"),
                @NamedAttributeNode("team2"),
                @NamedAttributeNode("stadium"),
                @NamedAttributeNode("lineup1"),
                @NamedAttributeNode("lineup2"),
                @NamedAttributeNode(value = "goals", subgraph = "goals-subgraph"),
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "goals-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("player"),
                                @NamedAttributeNode("assist")
                        }
                )
        })
@NamedEntityGraph(name = "Match.standings",
        attributeNodes = {
                @NamedAttributeNode("team1"),
                @NamedAttributeNode("team2")
        })
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long transfermarktId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team1;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team2;

    @ManyToMany
    @JsonIgnore
    private Set<Round> rounds = new HashSet<>();

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    @JsonView(MatchGoalsView.class)
    private List<Goal> goals = new ArrayList<>();

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    @JsonView(MatchMissedPenaltiesView.class)
    private List<MissedPenalty> missedPenalties = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<PlayerPoints> playerPoints = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Stadium stadium;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonView(MatchLineupsView.class)
    private Lineup lineup1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonView(MatchLineupsView.class)
    private Lineup lineup2;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    @JsonView(MatchCardsView.class)
    private List<Card> cards = new ArrayList<>();

    private Integer score1;

    private Integer score2;

    private Boolean reviewRequired = false;

    private LocalDateTime dateTime;
    
    public Match(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Team getTeam1() {
        return team1;
    }

    public void setTeam1(Team team1) {
        this.team1 = team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public void setTeam2(Team team2) {
        this.team2 = team2;
    }

    public Set<Round> getRounds() {
        return rounds;
    }

    public void setRounds(Set<Round> rounds) {
        this.rounds = rounds;
    }

    public List<Goal> getGoals() {
        return goals;
    }

    public void setGoals(List<Goal> goals) {
        this.goals = goals;
    }

    public Stadium getStadium() {
        return stadium;
    }

    public void setStadium(Stadium stadium) {
        this.stadium = stadium;
    }

    public Integer getScore1() {
        return score1;
    }

    public void setScore1(Integer score1) {
        this.score1 = score1;
    }

    public Integer getScore2() {
        return score2;
    }

    public void setScore2(Integer score2) {
        this.score2 = score2;
    }

    public Lineup getLineup1() {
        return lineup1;
    }

    public void setLineup1(Lineup lineup1) {
        this.lineup1 = lineup1;
    }

    public Lineup getLineup2() {
        return lineup2;
    }

    public void setLineup2(Lineup lineup2) {
        this.lineup2 = lineup2;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public Long getTransfermarktId() {
        return transfermarktId;
    }

    public void setTransfermarktId(Long transfermarktId) {
        this.transfermarktId = transfermarktId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public List<PlayerPoints> getPlayerPoints() {
        return playerPoints;
    }

    public void setPlayerPoints(List<PlayerPoints> playerPoints) {
        this.playerPoints = playerPoints;
    }

    public List<MissedPenalty> getMissedPenalties() {
        return missedPenalties;
    }

    public void setMissedPenalties(List<MissedPenalty> missedPenalties) {
        this.missedPenalties = missedPenalties;
    }

    public Boolean getReviewRequired() {
        return reviewRequired;
    }

    public void setReviewRequired(Boolean reviewRequired) {
        this.reviewRequired = reviewRequired;
    }

    public interface MatchGoalsView {}

    public interface MatchMissedPenaltiesView {}

    public interface MatchLineupsView {}

    public interface MatchCardsView {}

    public interface DetailedView extends MatchGoalsView, MatchLineupsView, MatchCardsView, MatchMissedPenaltiesView {}
}
