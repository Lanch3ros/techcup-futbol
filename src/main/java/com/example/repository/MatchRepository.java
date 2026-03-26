package com.example.repository;

import com.example.core.model.Match;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class MatchRepository {

    private final Map<Long, Match> matchDB = new HashMap<>();
    private long currentId = 1;

    public Match save(Match match) {
        if (match.getId() == null) {
            match.setId(currentId++);
        }
        matchDB.put(match.getId(), match);
        return match;
    }

    public List<Match> findAll() {
        return new ArrayList<>(matchDB.values());
    }

    public Match findById(Long id) {
        return matchDB.get(id);
    }

    public List<Match> findByTeamId(Long teamId) {
        return matchDB.values().stream()
                .filter(m -> (m.getHomeTeam() != null && m.getHomeTeam().getId().equals(teamId))
                        || (m.getAwayTeam() != null && m.getAwayTeam().getId().equals(teamId)))
                .collect(Collectors.toList());
    }
}