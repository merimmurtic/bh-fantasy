package com.fifa.wolrdcup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fifa.wolrdcup.model.players.Player;

import javax.persistence.*;

@Entity
public class Substitution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Lineup lineup;

    @ManyToOne(fetch = FetchType.LAZY)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    private Player substitutePlayer;

    private Integer minute;

    public Long getId() {
        return id;
    }
    public  Substitution(){}

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    public Lineup getLineup() {
        return lineup;
    }

    public void setLineup(Lineup lineup) {
        this.lineup = lineup;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getSubstitutePlayer() {
        return substitutePlayer;
    }

    public void setSubstitutePlayer(Player substitutePlayer) {
        this.substitutePlayer = substitutePlayer;
    }
}
