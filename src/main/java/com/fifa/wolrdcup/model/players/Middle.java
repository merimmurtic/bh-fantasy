package com.fifa.wolrdcup.model.players;

import com.fifa.wolrdcup.exception.InvalidPlayerPositionException;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Arrays;
import java.util.List;

@Entity
@DiscriminatorValue("Middle")
public class Middle extends Player {

    public Middle() {}

    public static List<Position> VALID_POSITIONS = Arrays.asList(
            Position.CM, Position.RM, Position.LM, Position.AM, Position.DM);

    public Middle(String firstName, String lastName, Integer numberoOnDress, Position position) throws Exception {
        super(firstName, lastName, numberoOnDress, position);
    }

    @Override
    public void validatePosition(Position position) {
        if(!VALID_POSITIONS.contains(position)) {
            throw new InvalidPlayerPositionException();
        }
    }
}
