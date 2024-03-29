package com.bhfantasy.web.model.players;

import com.bhfantasy.web.exception.InvalidPlayerPositionException;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Collections;
import java.util.List;

@Entity
@DiscriminatorValue("Goalkeaper")
public class Goalkeaper extends Player {

    public Goalkeaper() {}

    public Goalkeaper(String firstName, String lastName, Integer numberoOnDress, Position position) throws Exception {
        super(firstName, lastName, numberoOnDress, position);
    }

    public static List<Position> VALID_POSITIONS = Collections.singletonList(Position.GK);

    @Override
    public void validatePosition(Position position) {
        if(!VALID_POSITIONS.contains(position)) {
            throw new InvalidPlayerPositionException();
        }
    }
}
