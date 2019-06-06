package com.bhfantasy.web.model;

import com.bhfantasy.web.model.league.FantasyLeague;
import com.bhfantasy.web.model.league.RegularLeague;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class LeagueSetup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonView(LeagueSetupLeagueView.class)
    private RegularLeague league;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonView(LeagueSetupLeagueView.class)
    private FantasyLeague fantasyLeague;

    @ManyToMany
    @OrderBy("id")
    @JsonView(LeagueSetupLeagueSetupView.class)
    private Set<LeagueSetup> leagueSetups = new HashSet<>();

    private String transfermarktUrl;

    public LeagueSetup(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RegularLeague getLeague() {
        return league;
    }

    public void setLeague(RegularLeague league) {
        this.league = league;
    }

    public FantasyLeague getFantasyLeague() {
        return fantasyLeague;
    }

    public void setFantasyLeague(FantasyLeague fantasyLeague) {
        this.fantasyLeague = fantasyLeague;
    }

    public String getTransfermarktUrl() {
        return transfermarktUrl;
    }

    public void setTransfermarktUrl(String transfermarktUrl) {
        this.transfermarktUrl = transfermarktUrl;
    }

    public Set<LeagueSetup> getLeagueSetups() {
        return leagueSetups;
    }

    public void setLeagueSetups(Set<LeagueSetup> leagueSetups) {
        this.leagueSetups = leagueSetups;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public interface LeagueSetupLeagueView {}

    public interface LeagueSetupLeagueSetupView {}

    public interface DetailedView extends LeagueSetupLeagueView, LeagueSetupLeagueSetupView {}
}
