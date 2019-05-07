package com.fifa.wolrdcup.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fifa.wolrdcup.model.league.League;
import com.fifa.wolrdcup.model.players.Player;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany(mappedBy = "teams", cascade = CascadeType.ALL)
    @JsonView(TeamPlayersView.class)
    private List<Player> players = new ArrayList<>();

    @ManyToMany
    @JsonView(TeamLeaguesView.class)
    private List<League> leagues = new ArrayList<>();

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

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<League> getLeagues() {
        return leagues;
    }

    public void setLeagues(List<League> leagues) {
        this.leagues = leagues;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public interface TeamPlayersView {}

    public interface TeamLeaguesView {}

    public interface DetailedView extends TeamPlayersView, TeamLeaguesView{}
}
