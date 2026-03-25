package com.example.repository;

import com.example.core.model.Team;
import com.example.core.model.Tournament;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TournamentRepository {
    private final Map<Long, Tournament> tournamentDB = new HashMap<>();
    private long currentId = 1;

    public Tournament save(Tournament tournament) {
        if (tournament.getId() == null) {
            tournament.setId(currentId++);
        }
        tournamentDB.put(tournament.getId(), tournament);
        return tournament;
    }

    public List<Tournament> findAll() {
        return new ArrayList<>(tournamentDB.values());
    }

    public Tournament findById(Long id) {
        return tournamentDB.get(id);
    }
}
