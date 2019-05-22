package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.custom.TopPlayerValue;
import com.fifa.wolrdcup.model.league.RegularLeague;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegularLeagueRepository extends CrudRepository<RegularLeague, Long> {
    Optional<RegularLeague> findByNameAndSeason(String name, String season);

    @Query("select new com.fifa.wolrdcup.model.custom.TopPlayerValue(" +
            "player.id, trim(concat(coalesce(player.firstName, ''), ' ', coalesce(player.lastName, ''))), " +
            "team.profilePicture, count(goal), 0L, 0L) " +
            "from League league " +
            "join league.teams team " +
            "join team.players player " +
            "join player.goals goal " +
            "join goal.match match " +
            "join match.rounds round " +
            "join round.league roundLeague " +
            "where league.id = :leagueId and roundLeague.id = :leagueId " +
            "and (league.dtype = 'RegularLeague' or league.dtype = 'LeagueGroup') " +
            "group by player, team.profilePicture order by count(goal) desc")
    List<TopPlayerValue> getTopPlayers(@Param("leagueId") Long leagueId);

    @Query("select new com.fifa.wolrdcup.model.custom.TopPlayerValue(" +
            "assist.id, trim(concat(coalesce(assist.firstName, ''), ' ', coalesce(assist.lastName, ''))), " +
            "team.profilePicture, 0L, count(assist), 0L) " +
            "from League league " +
            "join league.teams team " +
            "join team.players player " +
            "join player.goals goal " +
            "join goal.assist assist " +
            "join goal.match match " +
            "join match.rounds round " +
            "join round.league roundLeague " +
            "where league.id = :leagueId and roundLeague.id = :leagueId " +
            "and (league.dtype = 'RegularLeague' or league.dtype = 'LeagueGroup') " +
            "group by assist, team.profilePicture order by count(assist) desc")
    List<TopPlayerValue> getTopPlayerAssists(@Param("leagueId") Long leagueId);

    @Query("select distinct new com.fifa.wolrdcup.model.custom.TopPlayerValue(" +
            "player.id, trim(concat(coalesce(player.firstName, ''), ' ', coalesce(player.lastName, ''))), " +
            "team.profilePicture, 0L, 0L, sum(playerPoints.points)) " +
            "from PlayerPoints playerPoints " +
            "join playerPoints.player player " +
            "join player.teams team " +
            "join team.leagues leagues " +
            "where leagues.id = :leagueId and (leagues.dtype = 'RegularLeague' or leagues.dtype = 'LeagueGroup') " +
            "group by player, team.profilePicture order by sum(playerPoints.points) desc")
    List<TopPlayerValue> getTopPlayersFantasyPoints(@Param("leagueId") Long leagueId);

    @Query("select league from League league where league.dtype = 'RegularLeague' or league.dtype = 'FantasyLeague'")
    Iterable<RegularLeague> getAllLeagues();
}
