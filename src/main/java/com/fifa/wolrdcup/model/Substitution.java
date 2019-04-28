package com.fifa.wolrdcup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class Substitution {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Lineup lineup;

    private Long playerId;

    private Long substittuionPlayerId;

    private Integer minute;

    public Long getId() {
        return id;
    }
    public  Substitution(){}

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public Long getSubstittuionPlayerId() {
        return substittuionPlayerId;
    }

    public void setSubstittuionPlayerId(Long substittuionPlayerId) {
        this.substittuionPlayerId = substittuionPlayerId;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }
}
