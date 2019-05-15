package com.fifa.wolrdcup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fifa.wolrdcup.model.players.Player;

import javax.persistence.*;

@Entity
public class MissedPenalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    private Player savedBy;

    private Integer minute;

    public MissedPenalty(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

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

    public Player getSavedBy() {
        return savedBy;
    }

    public void setSavedBy(Player savedBy) {
        this.savedBy = savedBy;
    }
}
