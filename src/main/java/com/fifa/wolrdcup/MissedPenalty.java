package com.fifa.wolrdcup;

import com.fifa.wolrdcup.model.Match;
import com.fifa.wolrdcup.model.players.Player;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class MissedPenalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToMany(mappedBy = "missedPenalty", cascade = CascadeType.ALL)
    private List<Match> match = new ArrayList<>();

    @OneToMany(mappedBy = "missedPenalty", cascade = CascadeType.ALL)
    private List<Player> player = new ArrayList<>();

    @OneToMany(mappedBy = "missedPenalty", cascade = CascadeType.ALL)
    private List<Player> concededBy  = new ArrayList<>();

    Integer minute;

   public MissedPenalty(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<Player> getConcededBy() {
        return concededBy;
    }

    public void setConcededBy(List<Player> concededBy) {
        this.concededBy = concededBy;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }
}
