package com.example.core.service;

import com.example.core.model.Team;
import com.example.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public Team createTeam(Team team) {
        log.info("Iniciando creación de equipo con nombre: {}", team.getName());

        Team savedTeam = teamRepository.save(team);

        log.info("Equipo creado exitosamente con ID: {}", savedTeam.getId());
        return savedTeam;
    }

    public List<Team> getAllTeams() {
        log.info("Consultando la lista de todos los equipos");
        return teamRepository.findAll();
    }

    public Team getTeamById(Long id) {
        log.info("Buscando equipo con ID: {}", id);
        return teamRepository.findById(id);
    }
}