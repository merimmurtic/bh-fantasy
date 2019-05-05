package com.fifa.wolrdcup.model;

import com.fifa.wolrdcup.model.players.Player;

import javax.persistence.*;

@Entity
public class PlayerPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer points;

    @ManyToOne
    private Match match;

    @ManyToOne
    private Player player;

    public PlayerPoints() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
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
}