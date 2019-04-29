package com.fifa.wolrdcup.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.fifa.wolrdcup.model.players.Player;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull // Make sure that name exist and prevent leagues with same name!
    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "leagues", cascade = CascadeType.ALL)
    @JsonView(LeagueTeamsView.class)
    private List<Team> teams = new LinkedList<>();

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL)
    @JsonView(LeagueRoundsView.class)
    private List<Round> rounds = new LinkedList<>();

    public League() {}

    public League(String name) {
        this.name = name;
    }

    public void addTeam(Team team){
        teams.add(team);
    }

    public List<Team> getTeams() {
        return teams;
    }

    @JsonView(LeaguePlayersView.class)
    public List<Player> getAllPlayers(){
        List<Player> allPlayers = new ArrayList<>();

        for(Team team : teams){
            allPlayers.addAll(team.getPlayers());
        }

        return allPlayers;
    }

    public List<Player> getPlayersPlayingOnPosition(Player.Position position){
        List<Player> playingOnPosition = new ArrayList<>();
        for(Team team : teams){
            for(Player player : team.getPlayers()){
                if(position == player.getPosition()){
                    playingOnPosition.add(player);
                }

            }
        }

        return playingOnPosition;
    }

    public List<Player> getPlayersWithDressNumber(Integer numberOnDress){
        List<Player> playingOnDress = new ArrayList<>();
        for(Team team : teams){
            playingOnDress.addAll(team.getPlayers().stream().filter(
                    player -> numberOnDress.equals(player.getNumberoOnDress())).collect(Collectors.toList()));
        }

        return playingOnDress;
    }

    public List<Player> searchPlayers(String value){
        List<Player> searchPlayers = new ArrayList<>();

        for(Team team : teams){
            searchPlayers.addAll(team.getPlayers().stream().filter(
                    player -> player.getFullName().toUpperCase().contains(value.toUpperCase())).collect(Collectors.toList()));
        }

        return searchPlayers;
    }

    public List<Player> getPlayersOfType(Class<? extends Player> type) {
        List<Player> result = new ArrayList<>();

        for(Team team : teams){
            result.addAll(team.getPlayers().stream().filter(
                    player -> player.getClass().equals(type)).collect(Collectors.toList()));
        }

        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public interface LeagueTeamsView {}

    public interface LeagueRoundsView {}

    public interface LeaguePlayersView {}

    public interface DetailedView extends LeagueTeamsView, LeaguePlayersView {}
}

