package com.fifa.wolrdcup.model;

public class Substitution {

    private Long id;

    private Long playerId;

    private Long substittuionPlayerId;

    private Integer minute;

    public Long getId() {
        return id;
    }

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
