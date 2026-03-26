package com.example.core.service;

import com.example.controller.dto.request.LineupRequest;
import com.example.core.exception.BusinessRuleException;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.Player;
import com.example.core.model.Team;
import com.example.repository.PlayerRepository;
import com.example.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    public TeamService(TeamRepository teamRepository, PlayerRepository playerRepository) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
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
        Team team = teamRepository.findById(id);
        if (team == null) throw new ResourceNotFoundException("Equipo con ID " + id + " no encontrado");
        return team;
    }

    public List<Player> getTeamPlayers(Long teamId) {
        log.info("Consultando jugadores del equipo {}", teamId);
        Team team = getTeamById(teamId);
        return team.getPlayers();
    }

    public void removePlayer(Long teamId, Long playerId) {
        log.info("Removiendo jugador {} del equipo {}", playerId, teamId);
        Team team = getTeamById(teamId);

        if (team.getPlayers() == null || team.getPlayers().stream().noneMatch(p -> p.getId().equals(playerId))) {
            throw new ResourceNotFoundException("El jugador con ID " + playerId + " no pertenece a este equipo");
        }

        team.getPlayers().removeIf(p -> p.getId().equals(playerId));

        Player player = playerRepository.findById(playerId);
        if (player != null) {
            player.setAvailable(true);
            player.setTeamId(null);
            playerRepository.save(player);
        }

        teamRepository.save(team);
        log.info("Jugador {} removido del equipo {} exitosamente", playerId, teamId);
    }

    public void sendInvitation(Long teamId, Long playerId) {
        log.info("Validando envío de invitación del equipo {} al jugador {}", teamId, playerId);

        Team team = teamRepository.findById(teamId);
        if (team == null) throw new ResourceNotFoundException("Equipo con ID " + teamId + " no encontrado");

        Player player = playerRepository.findById(playerId);
        if (player == null) throw new ResourceNotFoundException("Jugador con ID " + playerId + " no encontrado");

        if (!player.isAvailable()) throw new BusinessRuleException("El jugador ya tiene un equipo o no está disponible.");

        if (team.getPlayers() != null && team.getPlayers().size() >= 12) {
            throw new BusinessRuleException("El equipo ya alcanzó el límite máximo de 12 jugadores permitidos.");
        }

        log.info("Invitación generada y enviada con éxito al jugador {}.", playerId);
    }

    public LineupRequest getTeamLineup(Long teamId) {
        log.info("Consultando alineación del equipo {}", teamId);
        getTeamById(teamId);
        return null;
    }

    public void configureLineup(Long teamId, LineupRequest request) {
        log.info("Configurando alineación titular para el equipo {}", teamId);

        Team team = teamRepository.findById(teamId);
        if (team == null) throw new ResourceNotFoundException("Equipo con ID " + teamId + " no encontrado");

        if (team.getPlayers() == null || team.getPlayers().size() < 7) {
            throw new BusinessRuleException("El equipo no cumple con el mínimo de 7 jugadores requeridos.");
        }

        List<Long> startingIds = request.getStartingPlayersIds();
        for (Long playerId : startingIds) {
            boolean belongsToTeam = team.getPlayers().stream().anyMatch(p -> p.getId().equals(playerId));
            if (!belongsToTeam) {
                throw new BusinessRuleException("El jugador con ID " + playerId + " no pertenece a la plantilla de este equipo.");
            }
        }

        log.info("Alineación de {} jugadores configurada con formación {}.", startingIds.size(), request.getFormation());
    }
}