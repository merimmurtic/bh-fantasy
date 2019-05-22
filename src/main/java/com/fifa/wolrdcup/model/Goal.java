package com.fifa.wolrdcup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fifa.wolrdcup.model.players.Player;

import javax.persistence.*;

@Entity
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    private Player assist;

    private Integer minute;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    private Boolean penalty = false;

    private Boolean ownGoal = false;

    private Boolean ownAssist = false;

    private Integer score1;

    private Integer score2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Match match;

    public Goal() {}

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    public Boolean getPenalty() {
        return penalty;
    }

    public void setPenalty(Boolean penalty) {
        this.penalty = penalty;
    }

    public Boolean getOwnGoal() {
        return ownGoal;
    }

    public void setOwnGoal(Boolean ownGoal) {
        this.ownGoal = ownGoal;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Player getAssist() {
        return assist;
    }

    public void setAssist(Player assist) {
        this.assist = assist;
    }

    public Boolean getOwnAssist() {
        return ownAssist;
    }

    public void setOwnAssist(Boolean ownAssist) {
        this.ownAssist = ownAssist;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
