package com.example.repository;

import com.example.core.model.MatchEvent;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class MatchEventRepository {

    private final Map<Long, MatchEvent> eventDB = new HashMap<>();
    private long currentId = 1;

    public MatchEvent save(MatchEvent event) {
        if (event.getId() == null) {
            event.setId(currentId++);
        }
        eventDB.put(event.getId(), event);
        return event;
    }

    public List<MatchEvent> findByMatchId(Long matchId) {
        return eventDB.values().stream()
                .filter(e -> e.getMatchId().equals(matchId))
                .collect(Collectors.toList());
    }

    public List<MatchEvent> findAll() {
        return new ArrayList<>(eventDB.values());
    }
}