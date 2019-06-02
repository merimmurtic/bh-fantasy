package com.fifa.wolrdcup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.model.players.Player;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Team{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    private String profilePicture;

    @Column(unique = true)
    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String code;

    @ManyToMany(mappedBy = "teams")
    @JsonView(TeamPlayersView.class)
    @OrderBy("id")
    private Set<Player> players = new HashSet<>();

    @ManyToMany
    @JsonView(TeamLeaguesView.class)
    @OrderBy("id")
    private Set<League> leagues = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;

    public Team(){}

    public Team(String name, String code){
        this.name = name;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public void setPlayers(Set<Player> players) {
        this.players = players;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Set<League> getLeagues() {
        return leagues;
    }

    public void setLeagues(Set<League> leagues) {
        this.leagues = leagues;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Player> getPlayersOfType(Class<? extends Player> type) {
        return players.stream().filter(
                player -> player.getClass().equals(type)).collect(Collectors.toList());
    }

    public interface TeamPlayersView {}

    public interface TeamLeaguesView {}

    public interface DetailedView extends TeamPlayersView, TeamLeaguesView, League.LeagueGroupsView {}
}
