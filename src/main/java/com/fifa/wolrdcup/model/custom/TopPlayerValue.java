package com.fifa.wolrdcup.model.custom;

public class TopPlayerValue {

    private Long playerId;

    private String fullName;

    private  Long goalsScored;

    public TopPlayerValue(){}

    public TopPlayerValue(Long playerId, String fullName, Long goalsScored) {
        this.playerId = playerId;
        this.fullName = fullName;
        this.goalsScored = goalsScored;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public String getFullName() {
        return fullName;
    }

    public Long getGoalsScored() {
        return goalsScored;
    }

}
