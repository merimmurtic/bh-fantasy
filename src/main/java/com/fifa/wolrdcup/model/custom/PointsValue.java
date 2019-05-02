package com.fifa.wolrdcup.model.custom;

import com.fifa.wolrdcup.model.players.Middle;
import com.fifa.wolrdcup.model.players.Player;
import com.fifa.wolrdcup.model.players.Striker;

public class PointsValue {

    private Player player;

    private Integer goalsScored = 0;

    private Integer ownGoalsScored = 0;

    private Integer goalsAssisted = 0;

    private Integer minutesPlayed = 0;

    public PointsValue(Player player) {
        this.player = player;
    }

    public void addGoal() {
        goalsScored += 1;
    }

    public void addOwnGoal() {
        ownGoalsScored += 1;
    }

    public void addAssist() {
        goalsAssisted += 1;
    }

    public void setMinutesPlayed(Integer minutes) {
        minutesPlayed = minutes;
    }

    public Integer get() {
        int result = 0;

        result += goalsScored * getGoalScoredCoefficient();
        result += goalsAssisted * 3;
        result += ownGoalsScored * -2;

        if(minutesPlayed >= 60) {
            result += 1;
        }

        return result;
    }

    public Integer getGoalScoredCoefficient() {
        if(player instanceof Striker) {
            return 4;
        } else if(player instanceof Middle) {
            return 5;
        } else {
            return 6;
        }
    }

    @Override
    public String toString() {
        return String.format("Player %s has %d points.", player.getFullName(), get());
    }
}
