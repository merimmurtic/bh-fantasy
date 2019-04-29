package com.fifa.wolrdcup.model.custom;

public class TopPlayerValue {

    private Long playerId;

    private String fullName;

    private  Long goalsScored;

    public TopPlayerValue(){}

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Long getGoalsScored() {
        return goalsScored;
    }

    public void setGoalsScored(Long goalsScored) {
        this.goalsScored = goalsScored;
    }
}
