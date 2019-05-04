package com.fifa.wolrdcup.model.players;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fifa.wolrdcup.PlayerPoints;
import com.fifa.wolrdcup.model.Team;

import javax.persistence.*;
import java.sql.Date;
import java.util.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type", defaultImpl = Unknown.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Defender.class, name = "Defender"),
        @JsonSubTypes.Type(value = Goalkeaper.class, name = "Goalkeaper"),
        @JsonSubTypes.Type(value = Middle.class, name = "Middle"),
        @JsonSubTypes.Type(value = Striker.class, name = "Striker"),
        @JsonSubTypes.Type(value = Unknown.class, name = "Unknown")
})
public abstract class Player implements Comparable<Player> {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;


    private String firstName;
    private String lastName;
    private Integer numberoOnDress;

    private String marketValueRaw;

    private String profilePicture;

    private Date birthDate;

    @Column(unique = true)
    private Long transferMarktId;

    @Enumerated(EnumType.STRING)
    private Player.Position position;

    @ManyToMany
    @JsonView(PlayerTeamsView.class)
    private List<Team> teams = new ArrayList<>();

    @OneToMany
    @JsonIgnore
    private List<PlayerPoints> playerPoints = new ArrayList<>();

    public Player(){
    }

    Player(String firstName, String lastName, Integer numberoOnDress, Player.Position position) {
        validatePosition(position);

        this.firstName = firstName;
        this.lastName = lastName;
        this.numberoOnDress = numberoOnDress;
        this.position = position;
    }

    Player(String firstName, Team team){
        this.firstName = firstName;
    }

    public String getFirstName(){
        return  firstName;
    }
    public void setFirstName(String firstName){
        this.firstName = firstName;
    }
    public  String getLastName(){
        return  lastName;
    }
    public void setLastName(String lastName){
        this.lastName = lastName;
    }
    public Integer getNumberoOnDress(){
        return  numberoOnDress;
    }

    public void setNumberoOnDress(Integer numberoOnDress) {
        this.numberoOnDress = numberoOnDress;
    }

    public Player.Position getPosition() {
        return position;
    }

    public String getPositionName() {
        return position != null ? position.getName() : null;
    }

    public void setPosition(Player.Position position) {
        validatePosition(position);

        this.position = position;
    }

    public String getFullName() {
        if(firstName != null && lastName != null) {
            return String.format("%s %s", firstName, lastName);
        } else if(firstName != null) {
            return firstName;
        } else {
            return lastName;
        }
    }

    public String getMarketValueRaw() {
        return marketValueRaw;
    }

    public void setMarketValueRaw(String marketValueRaw) {
        this.marketValueRaw = marketValueRaw;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public abstract void validatePosition(Player.Position position);

    @Override
    public String toString() {
        return getFullName();
    }

    @Override
    public int compareTo(Player o) {
        return numberoOnDress - o.getNumberoOnDress();
    }

    public Long getTransferMarktId() {
        return transferMarktId;
    }

    public void setTransferMarktId(Long transferMarktId) {
        this.transferMarktId = transferMarktId;
    }

    public String getType() {
        // You can extend this method by returning type based on position if it exist.
        // Mapping between type and position needs to be created

        return this.getClass().getSimpleName();
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public List<PlayerPoints> getPlayerPoints() {
        return playerPoints;
    }

    public void setPlayerPoints(List<PlayerPoints> playerPoints) {
        this.playerPoints = playerPoints;
    }

    public enum Position{
        SS("Second Striker"),
        CF("Centre-Forward"),
        CM("Central Midfield"),
        CB("Centre-Back"),
        RB("Right-Back"),
        LB("Left-Back"),
        GK("Goalkeeper"),
        RM("Right Midfield"),
        LM("Left Midfield"),
        AM("Attacking Midfield"),
        DM("Defensive Midfield"),
        LW("Left Winger"),
        RW("Right Winger"),
        UNKNOWN("Unknown");

        Position(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        private String name;

        public static Position getPosition(String name){
            for(Position e : Position.values()){
                if(name.equals(e.name)) return e;
            }
            return UNKNOWN;
        }
    }

    public static Player getInstance(Position position) {
        if(Middle.VALID_POSITIONS.contains(position)) {
            return new Middle();
        } else if(Striker.VALID_POSITIONS.contains(position)) {
            return new Striker();
        } else if(Defender.VALID_POSITIONS.contains(position)) {
            return new Defender();
        } else if(Goalkeaper.VALID_POSITIONS.contains(position)) {
            return new Goalkeaper();
        } else return new Unknown();
    }


    public interface PlayerTeamsView {}

    public interface DetailedView extends PlayerTeamsView {}


}
