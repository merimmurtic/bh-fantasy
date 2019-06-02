package com.bhfantasy.web.model.players;

import com.bhfantasy.web.model.Team;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Unknown")
public class Unknown extends Player {

    public Unknown() {}

    public Unknown(String firstName, Team team) {
        super(firstName, team);
    }

    @Override
    public void validatePosition(Position position) {
    }
}