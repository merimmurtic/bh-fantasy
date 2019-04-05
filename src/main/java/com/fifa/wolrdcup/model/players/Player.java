package com.fifa.wolrdcup.model.players;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fifa.wolrdcup.model.Team;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.Map;

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

    @Enumerated(EnumType.STRING)
    private Player.Position position;

    private static Map<Player.Position, String> POSITIONS_MAP = new LinkedHashMap<>();

    static {
        POSITIONS_MAP.put(Player.Position.ST, "Striker");
        POSITIONS_MAP.put(Player.Position.MC, "Midller");
        POSITIONS_MAP.put(Player.Position.DC, "Defensive Center");
        POSITIONS_MAP.put(Player.Position.GK, "Goalkeeper");
        POSITIONS_MAP.put(Player.Position.AMC, "Ofansive Midle Center");

        for (Player.Position key : POSITIONS_MAP.keySet()) {
            String value = POSITIONS_MAP.get(key);
        }
    }

    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private Team team;

    @Column(name = "team_id", insertable = false, updatable = false)
    private Long teamId;

    @Column(name = "DTYPE", insertable = false, updatable = false)
    private String type;

    public Player(){
    }

    Player(String firstName, String lastName, Integer numberoOnDress, Player.Position position) throws Exception {
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
        return POSITIONS_MAP.getOrDefault(position, "Unknown");
    }

    public void setPosition(Player.Position position) throws Exception {
        validatePosition(position);

        this.position = position;
    }

    public String getFullName() {
        return lastName != null ? String.format("%s %s", firstName, lastName) : firstName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public abstract void validatePosition(Player.Position position) throws Exception;

    @Override
    public String toString() {
        return getFullName();
    }

    @Override
    public int compareTo(Player o) {
        return numberoOnDress - o.getNumberoOnDress();
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public enum Position{
        ST,
        MC,
        DC,
        DR,
        DL,
        GK,
        MR,
        ML,
        AMC,
        AML,
        AMR,
        FW,
        UNKNOWN
    }
}
