package com.fifa.wolrdcup.model.players;

import com.fifa.wolrdcup.exception.InvalidPlayerPositionException;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Arrays;
import java.util.List;

@Entity
@DiscriminatorValue("Defender")
public class Defender extends Player {

    public Defender() {}

    public Defender(String firstName, String lastName, Integer numberoOnDress, Position position) throws Exception {
        super(firstName, lastName, numberoOnDress, position);
    }

    public static List<Position> VALID_POSITIONS = Arrays.asList(Position.CB, Position.LB, Position.RB);

    @Override
    public void validatePosition(Position position) {
        if(!VALID_POSITIONS.contains(position)) {
            throw new InvalidPlayerPositionException();
        }
    }


}
