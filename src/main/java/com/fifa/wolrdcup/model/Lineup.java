package com.fifa.wolrdcup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fifa.wolrdcup.model.players.Player;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Lineup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Player> startingPlayers = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Player> availableSupstitutions = new ArrayList<>();

    @OneToMany(mappedBy = "lineup", cascade = CascadeType.ALL)
    private List<Substitution> substitutionChanges = new ArrayList<>();

    @OneToOne
    @JsonIgnore
    private Match match;

    @OneToOne
    private Player capiten;

    @OneToOne
    private Player viceCapiten;

    @Enumerated(EnumType.STRING)
    private Lineup.Formation formation;

    public Lineup(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Player getCapiten() {
        return capiten;
    }

    public void setCapiten(Player capiten) {
        this.capiten = capiten;
    }

    public Player getViceCapiten() {
        return viceCapiten;
    }

    public void setViceCapiten(Player viceCapiten) {
        this.viceCapiten = viceCapiten;
    }

    public List<Player> getStartingPlayers() {
        return startingPlayers;
    }

    public void setStartingPlayers(List<Player> startingPlayers) {
        this.startingPlayers = startingPlayers;
    }

    public List<Player> getAvailableSupstitutions() {
        return availableSupstitutions;
    }

    public void setAvailableSupstitutions(List<Player> availableSupstitutions) {
        this.availableSupstitutions = availableSupstitutions;
    }

    public List<Substitution> getSubstitutionChanges() {
        return substitutionChanges;
    }

    public void setSubstitutionChanges(List<Substitution> substitutionChanges) {
        this.substitutionChanges = substitutionChanges;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Formation getFormation() {
        return formation;
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
    }

    public enum Formation{
        F_4_3_3,
        F_4_4_2,
        F_5_4_1,
        F_4_5_1,
        F_4_4_1_1,
        F_4_3_2_1,
        F_5_3_2,
        F_3_4_3,
        F_3_5_2,
        F_3_4_2_1,
        F_4_2_3_1,
        F_4_1_4_1
    }
}
