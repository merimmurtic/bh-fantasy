package com.fifa.wolrdcup.model;

import com.fifa.wolrdcup.model.players.Player;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Lineup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @OrderBy("id")
    private Set<Player> startingPlayers = new HashSet<>();

    @ManyToMany
    @OrderBy("id")
    private Set<Player> availableSubstitutions = new HashSet<>();

    @OneToMany(mappedBy = "lineup")
    @OrderBy("id")
    private Set<Substitution> substitutionChanges = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Player capiten;

    @ManyToOne(fetch = FetchType.LAZY)
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

    public Set<Player> getStartingPlayers() {
        return startingPlayers;
    }

    public void setStartingPlayers(Set<Player> startingPlayers) {
        this.startingPlayers = startingPlayers;
    }

    public Set<Player> getAvailableSubstitutions() {
        return availableSubstitutions;
    }

    public void setAvailableSubstitutions(Set<Player> availableSubstitutions) {
        this.availableSubstitutions = availableSubstitutions;
    }

    public Set<Substitution> getSubstitutionChanges() {
        return substitutionChanges;
    }

    public void setSubstitutionChanges(Set<Substitution> substitutionChanges) {
        this.substitutionChanges = substitutionChanges;
    }

    public Formation getFormation() {
        return formation;
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
    }

    public List<Player> getPlayersOfType(Class<? extends Player> type) {
        return startingPlayers.stream().filter(
                player -> player.getClass().equals(type)).collect(Collectors.toList());
    }

    public enum Formation{
        F_5_3_2,
        F_5_4_1,
        F_4_3_3,
        F_4_4_2,
        F_4_2_4,
        F_4_5_1,
        F_4_4_1_1,
        F_4_3_2_1,
        F_4_2_3_1,
        F_4_1_4_1,
        F_4_3_1_2,
        F_4_1_3_2,
        F_4_1_2_3,
        F_4_1_2_1_2,
        F_4_6_0,
        F_4_2_2_2,
        F_4_2_1_3,
        F_3_1_4_2,
        F_3_5_2,
        F_3_4_3,
        F_3_4_2_1,
        F_3_4_1_2,
        F_3_3_4,
        F_3_6_1,
        F_3_3_1_3,
        F_3_3_3_1,
        F_2_3_5,
        F_2_3_2_3,
        F_1_6_3,
    }
}
