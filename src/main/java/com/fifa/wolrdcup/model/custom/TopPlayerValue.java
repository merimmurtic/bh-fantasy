package com.fifa.wolrdcup.model.custom;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopPlayerValue {

    private Long playerId;

    private String fullName;

    private String teamProfilePicture;

    private Long goalsScored;

    private Long points;

    private Long assistsMade;

    public TopPlayerValue(){}

    public TopPlayerValue(Long playerId, String fullName, String teamProfilePicture, Long goalsScored, Long assistsMade, Long points) {
        this.playerId = playerId;
        this.fullName = fullName;
        this.goalsScored = goalsScored;
        this.assistsMade = assistsMade;
        this.points = points;
        this.teamProfilePicture = teamProfilePicture;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getTeamProfilePicture() {
        return teamProfilePicture;
    }

    public Long getGoalsScored() {
        return goalsScored;
    }

    public Long getPoints() {
        return points;
    }

    public Long getAssistsMade() {
        return assistsMade;
    }

    public void setPoints(Long points) {
        this.points = points;
    }

    public void setAssistsMade(Long assistsMade) {
        this.assistsMade = assistsMade;
    }

    public void setGoalsScored(Long goalsScored) {
        this.goalsScored = goalsScored;
    }
}
