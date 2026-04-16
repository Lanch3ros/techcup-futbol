package com.example.core.service;

import com.example.controller.dto.request.LineupRequest;
import com.example.core.exception.BusinessRuleException;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.Invitation;
import com.example.core.model.Player;
import com.example.core.model.Program;
import com.example.core.model.Team;
import com.example.core.model.Tournament;
import com.example.core.model.User;
import com.example.repository.InvitationRepository;
import com.example.repository.TournamentRepository;
import com.example.repository.UserRepository;
import com.example.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final TournamentRepository tournamentRepository;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository,
                       InvitationRepository invitationRepository,
                       TournamentRepository tournamentRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
        this.tournamentRepository = tournamentRepository;
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
        List<Player> players = userRepository.findByTeamId(teamId).stream()
                .filter(u -> u instanceof Player)
                .map(u -> (Player) u)
                .collect(Collectors.toList());
        log.info("Total de jugadores en equipo ID {}: {}", teamId, players.size());
        return players;
    }

    public void removePlayer(Long teamId, Long playerId) {
        log.info("Removiendo jugador ID: {} del equipo ID: {}", playerId, teamId);
        Team team = getTeamById(teamId);

        // GAP-12: bloquear si el torneo está "En progreso"
        validateRosterNotFrozen(team);

        User player = userRepository.findById(playerId).orElseThrow(() -> {
            log.warn("Jugador ID: {} no encontrado", playerId);
            return new ResourceNotFoundException("El jugador con ID " + playerId + " no pertenece a este equipo");
        });

        if (!teamId.equals(player.getTeamId())) {
            log.warn("Jugador ID: {} no pertenece al equipo ID: {}", playerId, teamId);
            throw new ResourceNotFoundException("El jugador con ID " + playerId + " no pertenece a este equipo");
        }

        player.setAvailable(true);
        player.setTeamId(null);
        userRepository.save(player);

        log.info("Jugador ID: {} removido exitosamente del equipo ID: {}", playerId, teamId);
    }

    public void sendInvitation(Long teamId, Long playerId) {
        log.info("Validando envío de invitación del equipo ID: {} al jugador ID: {}", teamId, playerId);

        Team team = teamRepository.findById(teamId).orElseThrow(() -> {
            log.warn("Equipo no encontrado al enviar invitación - ID: {}", teamId);
            return new ResourceNotFoundException("Equipo con ID " + teamId + " no encontrado");
        });

        // GAP-12: bloquear si el torneo está "En progreso"
        validateRosterNotFrozen(team);

        User player = userRepository.findById(playerId).orElseThrow(() -> {
            log.warn("Jugador no encontrado al enviar invitación - ID: {}", playerId);
            return new ResourceNotFoundException("Jugador con ID " + playerId + " no encontrado");
        });

        // GAP-07: verificar explícitamente que el jugador no tenga equipo
        if (player.getTeamId() != null) {
            log.warn("Jugador ID: {} ya pertenece a un equipo (ID: {})", playerId, player.getTeamId());
            throw new BusinessRuleException("El jugador ya pertenece a un equipo.");
        }

        if (userRepository.countByTeamId(teamId) >= 12) {
            log.warn("Equipo ID: {} alcanzó el límite máximo de 12 jugadores", teamId);
            throw new BusinessRuleException("El equipo ya alcanzó el límite máximo de 12 jugadores permitidos.");
        }

        if (invitationRepository.existsByPlayerIdAndTeamIdAndStatusIgnoreCase(playerId, teamId, Invitation.PENDING)) {
            log.warn("Ya existe una invitación pendiente del equipo ID: {} al jugador ID: {}", teamId, playerId);
            throw new BusinessRuleException("Ya existe una invitación pendiente para este jugador de este equipo.");
        }

        Invitation invitation = new Invitation();
        invitation.setPlayerId(playerId);
        invitation.setTeamId(teamId);
        invitationRepository.save(invitation);

        log.info("Invitación persistida exitosamente - jugador ID: {}, equipo ID: {}", playerId, teamId);
    }

    // GAP-11: retorna la alineación persistida o null si no ha sido configurada
    public LineupRequest getTeamLineup(Long teamId) {
        log.info("Consultando alineación del equipo ID: {}", teamId);
        Team team = getTeamById(teamId);

        List<Long> starting = team.getStartingPlayerIds();
        if (starting == null || starting.isEmpty()) {
            log.info("No hay alineación configurada para el equipo ID: {}", teamId);
            return null;
        }

        LineupRequest lineup = new LineupRequest();
        lineup.setStartingPlayersIds(starting);
        lineup.setReservePlayerIds(team.getReservePlayerIds() != null ? team.getReservePlayerIds() : new ArrayList<>());
        lineup.setFormation(team.getFormation());
        log.info("Alineación retornada para equipo ID: {} - {} titulares, {} suplentes",
                teamId, starting.size(),
                lineup.getReservePlayerIds().size());
        return lineup;
    }

    public void configureLineup(Long teamId, LineupRequest request) {
        log.info("Configurando alineación para equipo ID: {}, formación: {}, titulares: {}",
                teamId, request.getFormation(), request.getStartingPlayersIds().size());

        Team team = teamRepository.findById(teamId).orElseThrow(() -> {
            log.warn("Equipo no encontrado al configurar alineación - ID: {}", teamId);
            return new ResourceNotFoundException("Equipo con ID " + teamId + " no encontrado");
        });

        List<Player> teamPlayers = userRepository.findByTeamId(teamId).stream()
                .filter(u -> u instanceof Player)
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

        // GAP-10: validar suplentes también pertenecen al equipo
        List<Long> reserveIds = request.getReservePlayerIds();
        if (reserveIds != null) {
            for (Long playerId : reserveIds) {
                boolean belongsToTeam = teamPlayers.stream().anyMatch(p -> p.getId().equals(playerId));
                if (!belongsToTeam) {
                    log.warn("Suplente ID: {} no pertenece a la plantilla del equipo ID: {}", playerId, teamId);
                    throw new BusinessRuleException("El suplente con ID " + playerId + " no pertenece a la plantilla de este equipo.");
                }
            }
        }

        validateEngineeringProgramComposition(teamPlayers, teamId);

        // GAP-10/11: persistir alineación en el equipo
        team.setStartingPlayerIds(new ArrayList<>(startingIds));
        team.setReservePlayerIds(reserveIds != null ? new ArrayList<>(reserveIds) : new ArrayList<>());
        team.setFormation(request.getFormation());
        teamRepository.save(team);

        log.info("Alineación configurada exitosamente para equipo ID: {} - {} titulares, {} suplentes, formación: '{}'",
                teamId, startingIds.size(),
                reserveIds != null ? reserveIds.size() : 0,
                request.getFormation());
    }

    // GAP-06: >50% ingeniería Y el resto EXCLUSIVAMENTE maestrías válidas (no null, no programa ajeno)
    private void validateEngineeringProgramComposition(List<Player> players, Long teamId) {
        if (players == null || players.isEmpty()) {
            return;
        }

        for (Player p : players) {
            if (!isValidProgram(p.getProgram())) {
                log.warn("Equipo ID: {} tiene jugador con programa inválido o nulo: {}", teamId, p.getProgram());
                throw new BusinessRuleException(
                        "Todos los jugadores deben pertenecer a un programa válido (Ingeniería o Maestría reconocida). " +
                        "Programa inválido detectado: " + p.getProgram());
            }
        }

        long engineeringCount = players.stream()
                .filter(p -> isEngineeringProgram(p.getProgram()))
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

    private boolean isValidProgram(Program program) {
        return program != null && (isEngineeringProgram(program) || isMasterProgram(program));
    }

    private boolean isEngineeringProgram(Program program) {
        return program == Program.SISTEMAS
                || program == Program.IA
                || program == Program.CIBERSEGURIDAD
                || program == Program.ESTADISTICA;
    }

    private boolean isMasterProgram(Program program) {
        return program == Program.MAESTRIA_GESTION_INFORMACION
                || program == Program.MAESTRIA_INFORMATICA
                || program == Program.MAESTRIA_CIENCIA_DATOS;
    }

    // GAP-12: lanza BusinessRuleException si el equipo está en un torneo "En progreso"
    private void validateRosterNotFrozen(Team team) {
        if (team.getTournamentId() == null) {
            return;
        }
        tournamentRepository.findById(team.getTournamentId()).ifPresent(tournament -> {
            if ("En progreso".equalsIgnoreCase(tournament.getStatus())) {
                log.warn("Intento de modificar plantilla del equipo ID: {} con torneo en progreso", team.getId());
                throw new BusinessRuleException(
                        "No se puede modificar la plantilla: el torneo ya está en progreso.");
            }
        });
    }

    public Team updateShieldUrl(Long teamId, String shieldUrl) {
        log.info("Actualizando escudo del equipo ID: {}", teamId);
        Team team = getTeamById(teamId);
        team.setShieldUrl(shieldUrl);
        Team updated = teamRepository.save(team);
        log.info("Escudo actualizado para equipo ID: {}", teamId);
        return updated;
    }
}
