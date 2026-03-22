package com.example.controller;

import com.example.controller.dto.request.TeamCreationRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.core.model.Team;
import com.example.core.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        try {
            teamService.createTeam(request.getName(), request.getColors());
            return new ResponseEntity<>(new GenericResponse("Éxito", "Equipo creado correctamente"), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new GenericResponse("Error", "No se pudo crear el equipo"), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Listar todos los equipos", description = "Obtiene una lista con todos los equipos registrados en el sistema.")
    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @Operation(summary = "Buscar equipo por ID", description = "Obtiene los detalles de un equipo específico.")
    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable Long id) {
        Team team = teamService.getTeamById(id);
        return team != null ? ResponseEntity.ok(team) : ResponseEntity.notFound().build();
    }
}