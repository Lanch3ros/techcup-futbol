package com.example.controller;

import com.example.controller.dto.request.LineupRequest;
import com.example.controller.dto.request.TeamCreationRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.controller.mapper.TeamMapper;
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
@Tag(name = "Equipos", description = "Endpoints para la creación y consulta de los equipos del torneo")
public class TeamController {

    private final TeamService teamService;
    private final TeamMapper teamMapper;

    public TeamController(TeamService teamService, TeamMapper teamMapper) {
        this.teamService = teamService;
        this.teamMapper = teamMapper;
    }

    @Operation(summary = "Crear un nuevo equipo", description = "Registra un equipo con su nombre y colores oficiales. El usuario que lo crea queda designado como capitán.")
    @PostMapping
    public ResponseEntity<GenericResponse> createTeam(@RequestBody @Valid TeamCreationRequest request) {
        log.info("Petición REST POST recibida en /api/v1/teams para crear equipo: {}", request.getName());

        try {
            Team teamEntity = teamMapper.toEntity(request);
            teamService.createTeam(teamEntity);

            log.info("Petición procesada exitosamente. Retornando código HTTP 201 (CREATED).");
            return new ResponseEntity<>(new GenericResponse("Éxito", "Equipo creado correctamente"), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error al procesar la petición de creación de equipo {}: {}", request.getName(), e.getMessage(), e);
            return new ResponseEntity<>(new GenericResponse("Error", "No se pudo crear el equipo"), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Listar todos los equipos", description = "Obtiene una lista con todos los equipos registrados en el sistema.")
    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        log.info("Petición REST GET recibida en /api/v1/teams");

        List<Team> teams = teamService.getAllTeams();
        log.info("Retornando lista con {} equipos. Código HTTP 200 (OK).", teams.size());

        return ResponseEntity.ok(teams);
    }

    @Operation(summary = "Buscar equipo por ID", description = "Obtiene los detalles de un equipo específico.")
    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable Long id) {
        log.info("Petición REST GET recibida en /api/v1/teams/{}", id);

        Team team = teamService.getTeamById(id);

        if (team != null) {
            log.info("Equipo encontrado. Retornando código HTTP 200 (OK).");
            return ResponseEntity.ok(team);
        } else {
            log.warn("Equipo no encontrado en la base de datos con ID: {}. Retornando código HTTP 404 (NOT FOUND).", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Enviar invitación a un jugador para unirse al equipo", description = "El capitán envía una solicitud a un jugador disponible usando su ID. El sistema validará que el equipo no exceda el máximo de 12 jugadores.")
    @PostMapping("/{id}/invitations")
    public ResponseEntity<GenericResponse> sendInvitation(@PathVariable Long id, @RequestBody Map<String, Long> payload) {
        log.info("Petición REST POST recibida en /api/v1/teams/{}/invitations", id);
        try {
            Long playerId = payload.get("playerId");
            if (playerId == null) {
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El campo 'playerId' es obligatorio"));
            }

            teamService.sendInvitation(id, playerId);
            log.info("Invitación enviada exitosamente del equipo {} al jugador {}.", id, playerId);

            return ResponseEntity.ok(new GenericResponse("Éxito", "Invitación enviada correctamente al jugador"));
        } catch (Exception e) {
            log.error("Error al enviar invitación del equipo {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }

    @Operation(summary = "Configurar alineación titular (Mín. 7 jugadores)", description = "Permite al capitán organizar su formación antes del partido, seleccionando los titulares y la formación táctica.")
    @PutMapping("/{id}/lineup")
    public ResponseEntity<GenericResponse> configureLineup(@PathVariable Long id, @RequestBody @Valid LineupRequest request) {
        log.info("Petición REST PUT recibida en /api/v1/teams/{}/lineup", id);
        try {
            teamService.configureLineup(id, request);
            log.info("Alineación configurada exitosamente para el equipo {}.", id);

            return ResponseEntity.ok(new GenericResponse("Éxito", "Alineación guardada correctamente"));
        } catch (Exception e) {
            log.error("Error al configurar la alineación del equipo {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }
}