package com.fifa.wolrdcup.model.players;

import com.fifa.wolrdcup.model.Team;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Collections;
import java.util.List;

@Entity
@DiscriminatorValue("Unknown")
public class Unknown extends Player {

    public Unknown() {}

    public Unknown(String firstName, Team team) {
        super(firstName, team);
    }

    private static List<Position> VALID_POSITIONS = Collections.singletonList(Position.UNKNOWN);

    @Override
    public void validatePosition(Position position) throws Exception {
        if(!VALID_POSITIONS.contains(position)) {
            throw new Exception("Nevalidna pozicija");
        }
    }


}