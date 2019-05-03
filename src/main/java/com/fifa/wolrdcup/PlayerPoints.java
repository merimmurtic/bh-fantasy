package com.fifa.wolrdcup;

import com.fifa.wolrdcup.model.Match;
import com.fifa.wolrdcup.model.players.Player;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PlayerPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private Integer points;

    @OneToMany(mappedBy = "playerPoints", cascade = CascadeType.ALL)
    private List<Match> match = new ArrayList<>();

    @OneToMany(mappedBy = "playerPoints", cascade = CascadeType.ALL)
    private List<Player> player = new ArrayList<>();

    public PlayerPoints(){}

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

    public List<Match> getMatch() {
        return match;
    }

    public void setMatch(List<Match> match) {
        this.match = match;
    }

    public List<Player> getPlayer() {
        return player;
    }

    public void setPlayer(List<Player> player) {
        this.player = player;
    }
}
