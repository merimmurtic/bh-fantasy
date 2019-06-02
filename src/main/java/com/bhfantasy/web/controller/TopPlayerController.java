package com.bhfantasy.web.controller;

import com.bhfantasy.web.model.custom.TopPlayerValue;
import com.bhfantasy.web.model.league.FantasyLeague;
import com.bhfantasy.web.model.league.League;
import com.bhfantasy.web.repository.LeagueRepository;
import com.bhfantasy.web.repository.RegularLeagueRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("leagues/{leagueId}/top-players")
public class TopPlayerController {

    private final LeagueRepository leagueRepository;

    private final RegularLeagueRepository regularLeagueRepository;

    public TopPlayerController(LeagueRepository leagueRepository, RegularLeagueRepository regularLeagueRepository) {
        this.leagueRepository = leagueRepository;
        this.regularLeagueRepository = regularLeagueRepository;
    }

    private Map<Long, TopPlayerValue> getGoalsAndAssistsMap(Long leagueId) {
        Map<Long, TopPlayerValue> resultMap = regularLeagueRepository.getTopPlayers(leagueId).stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.getPlayerId(), item), Map::putAll);

        Map<Long, TopPlayerValue> assistsMap = regularLeagueRepository.getTopPlayerAssists(leagueId).stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.getPlayerId(), item), Map::putAll);

        resultMap.forEach((playerId, value) -> {
            if(assistsMap.containsKey(playerId)) {
                value.setAssistsMade(assistsMap.get(playerId).getAssistsMade());
            }
        });

        return resultMap;
    }

    @GetMapping()
    public ResponseEntity<Collection<TopPlayerValue>> getTopPlayers(
            @PathVariable("leagueId") Long leagueId) {
        Optional<League> optionalLeague = leagueRepository.findById(leagueId);

        if(!optionalLeague.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        League league = optionalLeague.get();

        Long fantasyLeagueId = null;

        if(league instanceof FantasyLeague) {
            fantasyLeagueId = leagueId;
            leagueId = ((FantasyLeague) league).getRegularLeague().getId();
        }

        Map<Long, TopPlayerValue> resultMap = getGoalsAndAssistsMap(leagueId);

        if(fantasyLeagueId != null) {
            Map<Long, TopPlayerValue> pointsMap = regularLeagueRepository.getTopPlayersFantasyPoints(leagueId).stream()
                    .collect(LinkedHashMap::new, (map, item) -> map.put(item.getPlayerId(), item), Map::putAll);

            pointsMap.forEach((playerId, value) -> {
                if (resultMap.containsKey(playerId)) {
                    value.setGoalsScored(resultMap.get(playerId).getGoalsScored());
                    value.setAssistsMade(resultMap.get(playerId).getAssistsMade());
                }
            });

            return ResponseEntity.ok(pointsMap.values());
        } else {
            resultMap.forEach((playerId, value) -> {
                value.setPoints(null);
            });
        }

        return ResponseEntity.ok(resultMap.values());
    }
}
