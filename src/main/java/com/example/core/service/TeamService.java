package com.example.core.service;

import com.example.core.model.Team;
import com.example.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public Team createTeam(String name, String colors) {
        Team newTeam = new Team();
        newTeam.setName(name);
        newTeam.setColors(colors);
        return teamRepository.save(newTeam);
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(Long id) {
        return teamRepository.findById(id);
    }
}