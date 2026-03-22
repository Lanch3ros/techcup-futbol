package com.example.repository;

import com.example.core.model.Team;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TeamRepository {
    private final Map<Long, Team> teamsDB = new HashMap<>();
    private long currentId = 1;

    public Team save(Team team) {
        if (team.getId() == null) {
            team.setId(currentId++);
        }
        teamsDB.put(team.getId(), team);
        return team;
    }

    public List<Team> findAll() {
        return new ArrayList<>(teamsDB.values());
    }

    public Team findById(Long id) {
        return teamsDB.get(id);
    }
}