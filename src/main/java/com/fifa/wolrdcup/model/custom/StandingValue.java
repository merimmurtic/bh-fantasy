package com.fifa.wolrdcup.model.custom;

public class StandingValue{

    private Long teamId;

    private String teamName;

    private Long draw;

    private Long lose;

    private Long won;

    private Long goalsScored;

    private Long goalsConceded;

    private String profilePicture;

    public StandingValue() {}

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Long getPoints() {
        return won * 3 + draw;
    }

    public Long getDraw() {
        return draw;
    }

    public void setDraw(Long draw) {
        this.draw = draw;
    }

    public Long getLose() {
        return lose;
    }

    public void setLose(Long lose) {
        this.lose = lose;
    }

    public Long getWon() {
        return won;
    }

    public void setWon(Long won) {
        this.won = won;
    }

    public Long getGoalsScored() {
        return goalsScored;
    }

    public void setGoalsScored(Long goalsScored) {
        this.goalsScored = goalsScored;
    }

    public Long getGoalsConceded() {
        return goalsConceded;
    }

    public void setGoalsConceded(Long goalsConceded) {
        this.goalsConceded = goalsConceded;
    }

    public Long getGoalsDifference() {
        return goalsScored - goalsConceded;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
