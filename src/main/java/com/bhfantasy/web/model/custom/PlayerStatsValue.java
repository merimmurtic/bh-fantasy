package com.bhfantasy.web.model.custom;

public class PlayerStatsValue {

    private Integer started;

    private Integer enteredGame;

    private Integer leftGame;

    private Integer total;

    private Integer minutes;

    private Integer goals;

    private Integer assists;

    private Integer yellowCards;

    private Integer redCards;

    private Integer points;

    public PlayerStatsValue() {
    }

    public PlayerStatsValue(
            Integer started, Integer enteredGame, Integer leftGame,
            Integer goals, Integer assists, Integer yellowCards,
            Integer redCards, Integer points) {
        this.started = started;
        this.enteredGame = enteredGame;
        this.leftGame = leftGame;
        this.goals = goals;
        this.assists = assists;
        this.yellowCards = yellowCards;
        this.redCards = redCards;
        this.points = points;
    }

    public Integer getStarted() {
        return started;
    }

    public void setStarted(Integer started) {
        this.started = started;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public Integer getGoals() {
        return goals;
    }

    public void setGoals(Integer goals) {
        this.goals = goals;
    }

    public Integer getAssists() {
        return assists;
    }

    public void setAssists(Integer assists) {
        this.assists = assists;
    }

    public Integer getYellowCards() {
        return yellowCards;
    }

    public void setYellowCards(Integer yellowCards) {
        this.yellowCards = yellowCards;
    }

    public Integer getRedCards() {
        return redCards;
    }

    public void setRedCards(Integer redCards) {
        this.redCards = redCards;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getEnteredGame() {
        return enteredGame;
    }

    public void setEnteredGame(Integer enteredGame) {
        this.enteredGame = enteredGame;
    }

    public Integer getLeftGame() {
        return leftGame;
    }

    public void setLeftGame(Integer leftGame) {
        this.leftGame = leftGame;
    }
}
