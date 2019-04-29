package com.fifa.wolrdcup.model.players;

import com.fifa.wolrdcup.exception.InvalidPlayerPositionException;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Arrays;
import java.util.List;

@Entity
@DiscriminatorValue("Striker")
public class Striker extends Player{

    public Striker() {}

    public Striker(String firstName, String lastName, Integer numberoOnDress, Position position) {
        super(firstName, lastName, numberoOnDress, position);
    }

    public static List<Position> VALID_POSITIONS = Arrays.asList(Position.SS, Position.CF, Position.LW, Position.RW);

    @Override
    public void validatePosition(Position position) {
        if(!VALID_POSITIONS.contains(position)) {
            throw new InvalidPlayerPositionException();
        }
    }
}
