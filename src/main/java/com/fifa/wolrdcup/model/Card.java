package com.fifa.wolrdcup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Match match;

    private Long playerId;

    private Integer minute;

    public enum cardType{
        YELLOW,
        RED
    }
}
