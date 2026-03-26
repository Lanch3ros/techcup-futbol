package com.example.controller;

import com.example.controller.dto.request.LineupRequest;
import com.example.controller.dto.request.TeamCreationRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.controller.dto.response.ProfileDTO;
import com.example.controller.mapper.PlayerMapper;
import com.example.controller.mapper.TeamMapper;
import com.example.core.model.Player;
import com.example.core.model.Team;
import com.example.core.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/teams")
@Tag(name = "Equipos", description = "Endpoints para la creación y gestión de equipos del torneo")
public class TeamController {

    private final TeamService teamService;
    private final TeamMapper teamMapper;
    private final PlayerMapper playerMapper;

    public TeamController(TeamService teamService, TeamMapper teamMapper, PlayerMapper playerMapper) {
        this.teamService = teamService;
        this.teamMapper = teamMapper;
        this.playerMapper = playerMapper;
    }

    @Operation(summary = "Crear un nuevo equipo")
    @PostMapping
    public ResponseEntity<GenericResponse> createTeam(@RequestBody @Valid TeamCreationRequest request) {
        log.info("POST /api/v1/teams - nombre: {}", request.getName());
        try {
            Team teamEntity = teamMapper.toEntity(request);
            teamService.createTeam(teamEntity);
            return new ResponseEntity<>(new GenericResponse("Éxito", "Equipo creado correctamente"), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new GenericResponse("Error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }


    @Operation(summary = "Listar todos los equipos")
    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        log.info("GET /api/v1/teams");
        return ResponseEntity.ok(teamService.getAllTeams());
    }


    @Operation(summary = "Buscar equipo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable Long id) {
        log.info("GET /api/v1/teams/{}", id);
        try {
            return ResponseEntity.ok(teamService.getTeamById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(summary = "Listar jugadores de un equipo",
            description = "Retorna todos los jugadores que pertenecen al equipo especificado.")
    @GetMapping("/{id}/players")
    public ResponseEntity<List<ProfileDTO>> getTeamPlayers(@PathVariable Long id) {
        log.info("GET /api/v1/teams/{}/players", id);
        try {
            List<Player> players = teamService.getTeamPlayers(id);
            if (players == null) return ResponseEntity.ok(List.of());
            List<ProfileDTO> profiles = players.stream().map(playerMapper::toDto).toList();
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @Operation(summary = "Consultar la alineación actual del equipo",
            description = "Retorna la formación y titulares configurados por el capitán.")
    @GetMapping("/{id}/lineup")
    public ResponseEntity<GenericResponse> getTeamLineup(@PathVariable Long id) {
        log.info("GET /api/v1/teams/{}/lineup", id);
        try {
            Object lineup = teamService.getTeamLineup(id);
            if (lineup == null) {
                return ResponseEntity.ok(new GenericResponse("Info", "No hay alineación configurada para este equipo"));
            }
            return ResponseEntity.ok(new GenericResponse("Éxito", lineup));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Remover un jugador del equipo",
            description = "El capitán puede retirar a un jugador de la plantilla. El jugador quedará disponible nuevamente.")
    @DeleteMapping("/{id}/players/{playerId}")
    public ResponseEntity<GenericResponse> removePlayer(@PathVariable Long id, @PathVariable Long playerId) {
        log.info("DELETE /api/v1/teams/{}/players/{}", id, playerId);
        try {
            teamService.removePlayer(id, playerId);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Jugador removido del equipo correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Enviar invitación a un jugador")
    @PostMapping("/{id}/invitations")
    public ResponseEntity<GenericResponse> sendInvitation(@PathVariable Long id, @RequestBody Map<String, Long> payload) {
        log.info("POST /api/v1/teams/{}/invitations", id);
        try {
            Long playerId = payload.get("playerId");
            if (playerId == null) {
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El campo 'playerId' es obligatorio"));
            }
            teamService.sendInvitation(id, playerId);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Invitación enviada correctamente al jugador"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Actualizar estado de pago del equipo",
            description = "Permite actualizar el estado del pago de inscripción de un equipo.")
    @PatchMapping("/{id}/payment")
    public ResponseEntity<GenericResponse> updatePaymentStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        log.info("PATCH /api/v1/teams/{}/payment", id);
        try {
            String status = payload.get("status");
            if (status == null || status.isBlank()) {
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El campo 'status' es obligatorio"));
            }
            Team team = teamService.getTeamById(id);
            team.setPaymentStatus(status);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Estado de pago actualizado a: " + status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Configurar alineación titular (Mín. 7 jugadores)")
    @PutMapping("/{id}/lineup")
    public ResponseEntity<GenericResponse> configureLineup(@PathVariable Long id, @RequestBody @Valid LineupRequest request) {
        log.info("PUT /api/v1/teams/{}/lineup", id);
        try {
            teamService.configureLineup(id, request);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Alineación guardada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }
}