package com.bhfantasy.web.model.league;

import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("RegularLeague")
@Table(
        uniqueConstraints=@UniqueConstraint(columnNames={"name", "season"})
)
@NamedEntityGraph(name = "RegularLeague.withGroups",
        attributeNodes = {
                @NamedAttributeNode("groups")
        })
public class RegularLeague extends LeagueGroup{

    private String season;

    @ManyToMany
    @JsonView(LeagueGroupsView.class)
    @OrderBy("id")
    private Set<LeagueGroup> groups = new HashSet<>();

    private Level level;

    public RegularLeague(){}

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public Set<LeagueGroup> getGroups() {
        return groups;
    }

    public void setGroups(Set<LeagueGroup> groups) {
        this.groups = groups;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public enum Level {
        NATION,
        CLUB
    }
}
