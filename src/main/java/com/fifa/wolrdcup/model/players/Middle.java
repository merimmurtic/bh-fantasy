package com.fifa.wolrdcup.model.players;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Arrays;
import java.util.List;

@Entity
@DiscriminatorValue("Middle")
public class Middle extends Player {

    public Middle() {}

    private static List<Position> VALID_POSITIONS = Arrays.asList(
            Position.MC, Position.MR, Position.ML, Position.AMC, Position.AML, Position.AMR);

    public Middle(String firstName, String lastName, Integer numberoOnDress, Position position) throws Exception {
        super(firstName, lastName, numberoOnDress, position);
    }

    @Override
    public void validatePosition(Position position) throws Exception {
        if(!VALID_POSITIONS.contains(position)) {
            throw new Exception("Nevalidna pozicija");
        }
    }
}
