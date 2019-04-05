package com.fifa.wolrdcup.model.players;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Arrays;
import java.util.List;

@Entity
@DiscriminatorValue("Striker")
public class Striker extends Player{

    public Striker() {}

    public Striker(String firstName, String lastName, Integer numberoOnDress, Position position) throws Exception {
        super(firstName, lastName, numberoOnDress, position);
    }

    private static List<Position> VALID_POSITIONS = Arrays.asList(Position.ST, Position.FW);

    @Override
    public void validatePosition(Position position) throws Exception {
        if(!VALID_POSITIONS.contains(position)) {
            throw new Exception("Nevalidna pozicija");
        }
    }
}
