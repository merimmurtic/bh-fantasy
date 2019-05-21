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
            "from Goal goal " +
            "join goal.player player " +
            "join player.teams team " +
            "join team.leagues league " +
            "where league.id = :leagueId and league.dtype = 'RegularLeague' " +
            "group by player, team.profilePicture order by count(goal) desc")
    List<TopPlayerValue> getTopPlayers(@Param("leagueId") Long leagueId);

    @Query("select new com.fifa.wolrdcup.model.custom.TopPlayerValue(" +
            "player.id, trim(concat(coalesce(player.firstName, ''), ' ', coalesce(player.lastName, ''))), " +
            "team.profilePicture, 0L, count(goal), 0L) " +
            "from Goal goal " +
            "join Player player on goal.assist.id = player.id " +
            "join player.teams team " +
            "join team.leagues league " +
            "where league.id = :leagueId and league.dtype = 'RegularLeague' " +
            "group by player, team.profilePicture order by count(goal) desc")
    List<TopPlayerValue> getTopPlayerAssists(@Param("leagueId") Long leagueId);

    @Query("select distinct new com.fifa.wolrdcup.model.custom.TopPlayerValue(" +
            "player.id, trim(concat(coalesce(player.firstName, ''), ' ', coalesce(player.lastName, ''))), " +
            "team.profilePicture, 0L, 0L, sum(playerPoints.points)) " +
            "from PlayerPoints playerPoints " +
            "join playerPoints.player player " +
            "join player.teams team " +
            "join team.leagues leagues " +
            "where leagues.id = :leagueId and leagues.dtype = 'RegularLeague' " +
            "group by player, team.profilePicture order by sum(playerPoints.points) desc")
    List<TopPlayerValue> getTopPlayersFantasyPoints(@Param("leagueId") Long leagueId);

    @Query("select distinct g from RegularLeague league " +
            "inner join league.groups g " +
            "inner join fetch g.teams team where league.id = :leagueId")
    List<RegularLeague> getGroupsWithTeams(@Param("leagueId") Long leagueId);

    @Query("select g from RegularLeague league " +
            "inner join league.groups g " +
            "where league.id = :leagueId and g.name = :name")
    Optional<RegularLeague> findGroup(@Param("leagueId") Long leagueId, @Param("name") String name);
}
