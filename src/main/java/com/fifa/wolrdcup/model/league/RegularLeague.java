package com.fifa.wolrdcup.model.league;

import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("RegularLeague")
@Table(
        uniqueConstraints=@UniqueConstraint(columnNames={"name", "season"})
)
public class RegularLeague extends LeagueGroup{

    private String season;

    @ManyToMany
    @JsonView(LeagueGroupsView.class)
    private List<LeagueGroup> groups = new ArrayList<>();

    public RegularLeague(){}

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public List<LeagueGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<LeagueGroup> groups) {
        this.groups = groups;
    }
}
