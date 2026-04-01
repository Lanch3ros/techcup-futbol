package com.example.core.service;

import com.example.controller.dto.request.LineupRequest;
import com.example.core.exception.BusinessRuleException;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.Player;
import com.example.core.model.Program;
import com.example.core.model.Team;
import com.example.core.model.User;
import com.example.repository.PlayerRepository;
import com.example.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        log.info("Iniciando creación de equipo con nombre: '{}'", team.getName());
        Team savedTeam = teamRepository.save(team);
        log.info("Equipo creado exitosamente - ID: {}, nombre: '{}'", savedTeam.getId(), savedTeam.getName());
        return savedTeam;
    }

    public List<Team> getAllTeams() {
        log.info("Consultando la lista de todos los equipos");
        List<Team> teams = teamRepository.findAll();
        log.info("Total de equipos obtenidos: {}", teams.size());
        return teams;
    }

    public Team getTeamById(Long id) {
        log.info("Buscando equipo con ID: {}", id);
        Team team = teamRepository.findById(id).orElseThrow(() -> {
            log.warn("Equipo no encontrado - ID: {}", id);
            return new ResourceNotFoundException("Equipo con ID " + id + " no encontrado");
        });
        log.info("Equipo encontrado - ID: {}, nombre: '{}'", id, team.getName());
        return team;
    }

    public List<Player> getTeamPlayers(Long teamId) {
        log.info("Consultando jugadores del equipo ID: {}", teamId);
        getTeamById(teamId);
        List<Player> players = playerRepository.findByTeamId(teamId).stream()
                .map(u -> (Player) u)
                .collect(Collectors.toList());
        log.info("Total de jugadores en equipo ID {}: {}", teamId, players.size());
        return players;
    }

    public void removePlayer(Long teamId, Long playerId) {
        log.info("Removiendo jugador ID: {} del equipo ID: {}", playerId, teamId);
        getTeamById(teamId);

        User player = playerRepository.findById(playerId).orElseThrow(() -> {
            log.warn("Jugador ID: {} no encontrado", playerId);
            return new ResourceNotFoundException("El jugador con ID " + playerId + " no pertenece a este equipo");
        });

        if (!teamId.equals(player.getTeamId())) {
            log.warn("Jugador ID: {} no pertenece al equipo ID: {}", playerId, teamId);
            throw new ResourceNotFoundException("El jugador con ID " + playerId + " no pertenece a este equipo");
        }

        player.setAvailable(true);
        player.setTeamId(null);
        playerRepository.save(player);

        log.info("Jugador ID: {} removido exitosamente del equipo ID: {}", playerId, teamId);
    }

    public void sendInvitation(Long teamId, Long playerId) {
        log.info("Validando envío de invitación del equipo ID: {} al jugador ID: {}", teamId, playerId);

        Team team = teamRepository.findById(teamId).orElseThrow(() -> {
            log.warn("Equipo no encontrado al enviar invitación - ID: {}", teamId);
            return new ResourceNotFoundException("Equipo con ID " + teamId + " no encontrado");
        });

        User player = playerRepository.findById(playerId).orElseThrow(() -> {
            log.warn("Jugador no encontrado al enviar invitación - ID: {}", playerId);
            return new ResourceNotFoundException("Jugador con ID " + playerId + " no encontrado");
        });

        if (!player.isAvailable()) {
            log.warn("Jugador ID: {} no disponible para recibir invitación", playerId);
            throw new BusinessRuleException("El jugador ya tiene un equipo o no está disponible.");
        }

        if (playerRepository.countByTeamId(teamId) >= 12) {
            log.warn("Equipo ID: {} alcanzó el límite máximo de 12 jugadores", teamId);
            throw new BusinessRuleException("El equipo ya alcanzó el límite máximo de 12 jugadores permitidos.");
        }

        log.info("Invitación enviada exitosamente al jugador ID: {} desde equipo ID: {}", playerId, teamId);
    }

    public LineupRequest getTeamLineup(Long teamId) {
        log.info("Consultando alineación del equipo ID: {}", teamId);
        getTeamById(teamId);
        log.warn("No hay alineación configurada para el equipo ID: {}", teamId);
        return null;
    }

    public void configureLineup(Long teamId, LineupRequest request) {
        log.info("Configurando alineación para equipo ID: {}, formación: {}, jugadores: {}", teamId, request.getFormation(), request.getStartingPlayersIds().size());

        teamRepository.findById(teamId).orElseThrow(() -> {
            log.warn("Equipo no encontrado al configurar alineación - ID: {}", teamId);
            return new ResourceNotFoundException("Equipo con ID " + teamId + " no encontrado");
        });

        List<Player> teamPlayers = playerRepository.findByTeamId(teamId).stream()
                .map(u -> (Player) u)
                .collect(Collectors.toList());

        if (teamPlayers.size() < 7) {
            log.warn("Equipo ID: {} no cumple el mínimo de 7 jugadores - total actual: {}", teamId, teamPlayers.size());
            throw new BusinessRuleException("El equipo no cumple con el mínimo de 7 jugadores requeridos.");
        }

        List<Long> startingIds = request.getStartingPlayersIds();
        for (Long playerId : startingIds) {
            boolean belongsToTeam = teamPlayers.stream().anyMatch(p -> p.getId().equals(playerId));
            if (!belongsToTeam) {
                log.warn("Jugador ID: {} no pertenece a la plantilla del equipo ID: {}", playerId, teamId);
                throw new BusinessRuleException("El jugador con ID " + playerId + " no pertenece a la plantilla de este equipo.");
            }
        }

        validateEngineeringProgramComposition(teamPlayers, teamId);

        log.info("Alineación configurada exitosamente para equipo ID: {} - {} jugadores, formación: '{}'", teamId, startingIds.size(), request.getFormation());
    }

    // RN-03-4: más del 50% de los jugadores deben pertenecer a programas de ingeniería
    private void validateEngineeringProgramComposition(List<Player> players, Long teamId) {
        if (players == null || players.isEmpty()) {
            return;
        }
        long engineeringCount = players.stream()
                .filter(p -> p.getProgram() != null && isEngineeringProgram(p.getProgram()))
                .count();
        double ratio = (double) engineeringCount / players.size();
        if (ratio <= 0.5) {
            log.warn("Equipo ID: {} no cumple RN-03-4: {}/{} jugadores de programas de ingeniería ({} %)",
                    teamId, engineeringCount, players.size(), (int) (ratio * 100));
            throw new BusinessRuleException(
                    "Más del 50% de los jugadores debe pertenecer a un programa de ingeniería (Sistemas, IA, Ciberseguridad, Estadística).");
        }
        log.info("Equipo ID: {} cumple RN-03-4: {}/{} jugadores de ingeniería", teamId, engineeringCount, players.size());
    }

    private boolean isEngineeringProgram(Program program) {
        return program == Program.SISTEMAS
                || program == Program.IA
                || program == Program.CIBERSEGURIDAD
                || program == Program.ESTADISTICA;
    }
}