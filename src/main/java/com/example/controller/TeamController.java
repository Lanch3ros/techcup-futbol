package com.example.controller;

import com.example.controller.dto.request.TeamCreationRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.core.model.Team;
import com.example.core.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/teams")
@Tag(name = "Equipos", description = "Endpoints para la creación y consulta de los equipos del torneo")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @Operation(summary = "Crear un nuevo equipo", description = "Registra un equipo con su nombre y colores oficiales.")
    @PostMapping
    public ResponseEntity<GenericResponse> createTeam(@RequestBody TeamCreationRequest request) {
        log.info("Petición REST POST recibida en /api/v1/teams para crear equipo: {}", request.getName());

        try {
            teamService.createTeam(request.getName(), request.getColors());
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
}