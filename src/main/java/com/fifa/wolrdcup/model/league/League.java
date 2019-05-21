package com.fifa.wolrdcup.model.league;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fifa.wolrdcup.model.Team;
import com.fifa.wolrdcup.model.players.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type", defaultImpl = RegularLeague.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegularLeague.class, name = "RegularLeague"),
        @JsonSubTypes.Type(value = FantasyLeague.class, name = "FantasyLeague"),
        @JsonSubTypes.Type(value = LeagueGroup.class, name = "LeagueGroup")
})
@NamedEntityGraph(name = "League.detail",
        attributeNodes = {
            @NamedAttributeNode("teams")
        })
public abstract class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @ManyToMany(mappedBy = "leagues")
    @JsonView(LeagueTeamsView.class)
    @OrderBy("id")
    private Set<Team> teams = new HashSet<>();

    @Transient
    private Long currentRoundId = null;

    @Column(insertable = false, updatable = false)
    private String dtype;

    public League() {}

    public League(String name) {
        this.name = name;
    }

    public void addTeam(Team team){
        teams.add(team);
    }

    public Set<Team> getTeams() {
        return teams;
    }

    @JsonIgnore
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof League)) return false;

        League that = (League) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public String getType() {
        return this.getClass().getSimpleName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTeams(Set<Team> teams) {
        this.teams = teams;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCurrentRoundId() {
        return currentRoundId;
    }

    public void setCurrentRoundId(Long currentRoundId) {
        this.currentRoundId = currentRoundId;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    public interface LeagueTeamsView {}

    public interface LeagueRoundsView {}

    public interface LeaguePlayersView {}

    public interface LeagueLineupsView {}

    public interface LeagueGroupsView {}

    public interface DetailedView extends LeagueTeamsView, LeaguePlayersView, LeagueLineupsView, LeagueGroupsView {}
}

