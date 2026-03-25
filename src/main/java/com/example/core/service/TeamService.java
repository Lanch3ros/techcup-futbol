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
        return teamRepository.findById(id);
    }


    public void sendInvitation(Long teamId, Long playerId) {
        log.info("Validando envío de invitación del equipo {} al jugador {}", teamId, playerId);

        Team team = teamRepository.findById(teamId);
        if (team == null) {
            log.error("Fallo al enviar invitación: Equipo {} no encontrado", teamId);
            throw new ResourceNotFoundException("Equipo con ID " + teamId + " no encontrado");
        }

        Player player = playerRepository.findById(playerId);
        if (player == null) {
            log.error("Fallo al enviar invitación: Jugador {} no encontrado", playerId);
            throw new ResourceNotFoundException("Jugador con ID " + playerId + " no encontrado");
        }

        if (!player.isAvailable()) {
            log.warn("El jugador {} ya pertenece a un equipo o no está disponible.", playerId);
            throw new BusinessRuleException("El jugador ya tiene un equipo o no está en la lista de agentes libres.");
        }

        if (team.getPlayers() != null && team.getPlayers().size() >= 12) {
            log.warn("El equipo {} intentó invitar a un jugador pero ya tiene 12 miembros.", teamId);
            throw new BusinessRuleException("El equipo ya alcanzó el límite máximo de 12 jugadores permitidos.");
        }

        log.info("Invitación generada y enviada con éxito al jugador {}.", playerId);
    }

    public void configureLineup(Long teamId, LineupRequest request) {
        log.info("Configurando alineación titular para el equipo {}", teamId);

        Team team = teamRepository.findById(teamId);
        if (team == null) {
            log.error("Fallo al configurar alineación: Equipo {} no encontrado", teamId);
            throw new ResourceNotFoundException("Equipo con ID " + teamId + " no encontrado");
        }

        if (team.getPlayers() == null || team.getPlayers().size() < 7) {
            log.warn("El equipo {} no tiene suficientes jugadores para armar una alineación.", teamId);
            throw new BusinessRuleException("El equipo no cumple con el mínimo de 7 jugadores requeridos para jugar.");
        }

        List<Long> startingIds = request.getStartingPlayersIds();
        for (Long playerId : startingIds) {
            boolean belongsToTeam = team.getPlayers().stream()
                    .anyMatch(p -> p.getId().equals(playerId));

            if (!belongsToTeam) {
                log.warn("Intento de alinear a un jugador (ID: {}) que no pertenece al equipo {}.", playerId, teamId);
                throw new BusinessRuleException("El jugador con ID " + playerId + " no pertenece a la plantilla de este equipo.");
            }
        }

        log.info("Alineación de {} jugadores configurada correctamente con formación {}.", startingIds.size(), request.getFormation());
    }
}