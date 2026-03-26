package com.example.controller;

import com.example.controller.dto.request.TournamentCreationRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.controller.dto.response.StandingDTO;
import com.example.controller.mapper.TournamentMapper;
import com.example.core.model.Match;
import com.example.core.model.Team;
import com.example.core.model.Tournament;
import com.example.core.service.StatsService;
import com.example.core.service.TournamentService;
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
@RequestMapping("/api/v1/tournaments")
@Tag(name = "Torneos", description = "Endpoints para la gestión y configuración del torneo")
public class TournamentController {

    private final TournamentService tournamentService;
    private final TournamentMapper tournamentMapper;
    private final StatsService statsService;

    public TournamentController(TournamentService tournamentService,
                                TournamentMapper tournamentMapper,
                                StatsService statsService) {
        this.tournamentService = tournamentService;
        this.tournamentMapper = tournamentMapper;
        this.statsService = statsService;
    }

    @Operation(summary = "Crear un nuevo torneo")
    @PostMapping
    public ResponseEntity<GenericResponse> createTournament(@RequestBody @Valid TournamentCreationRequest request) {
        log.info("POST /api/v1/tournaments");
        try {
            Tournament tournamentEntity = tournamentMapper.toEntity(request);
            tournamentService.createTournament(tournamentEntity);
            return new ResponseEntity<>(new GenericResponse("Éxito", "Torneo creado correctamente en estado Borrador"), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new GenericResponse("Error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Listar todos los torneos",
            description = "Retorna todos los torneos registrados en el sistema.")
    @GetMapping
    public ResponseEntity<List<Tournament>> getAllTournaments() {
        log.info("GET /api/v1/tournaments");
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    @Operation(summary = "Consultar un torneo por ID",
            description = "Retorna la información completa de un torneo específico.")
    @GetMapping("/{id}")
    public ResponseEntity<Tournament> getTournamentById(@PathVariable Long id) {
        log.info("GET /api/v1/tournaments/{}", id);
        try {
            return ResponseEntity.ok(tournamentService.getTournamentById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(summary = "Consultar el reglamento del torneo")
    @GetMapping("/{id}/rules")
    public ResponseEntity<GenericResponse> getTournamentRules(@PathVariable Long id) {
        log.info("GET /api/v1/tournaments/{}/rules", id);
        try {
            Tournament tournament = tournamentService.getTournamentById(id);
            return ResponseEntity.ok(new GenericResponse("Reglamento", tournament.getRegulations()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Ver equipos inscritos en el torneo",
            description = "Retorna la lista de todos los equipos registrados en un torneo.")
    @GetMapping("/{id}/teams")
    public ResponseEntity<List<Team>> getTournamentTeams(@PathVariable Long id) {
        log.info("GET /api/v1/tournaments/{}/teams", id);
        try {
            return ResponseEntity.ok(tournamentService.getTournamentTeams(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @Operation(summary = "Tabla de posiciones del torneo",
            description = "Calcula y retorna la tabla de posiciones actualizada del torneo.")
    @GetMapping("/{id}/standings")
    public ResponseEntity<List<StandingDTO>> getTournamentStandings(@PathVariable Long id) {
        log.info("GET /api/v1/tournaments/{}/standings", id);
        try {
            return ResponseEntity.ok(statsService.getTournamentStandings(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @Operation(summary = "Llaves eliminatorias del torneo",
            description = "Retorna los partidos organizados en formato de llave eliminatoria.")
    @GetMapping("/{id}/bracket")
    public ResponseEntity<GenericResponse> getTournamentBracket(@PathVariable Long id) {
        log.info("GET /api/v1/tournaments/{}/bracket", id);
        try {
            List<Match> matches = tournamentService.getTournamentById(id).getMatches();
            if (matches == null || matches.isEmpty()) {
                return ResponseEntity.ok(new GenericResponse("Info", "No se han generado partidos para este torneo todavía"));
            }
            return ResponseEntity.ok(new GenericResponse("Llaves", matches));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Inscribir equipo al torneo",
            description = "Registra un equipo en el torneo. El torneo debe estar en estado 'Activo'.")
    @PostMapping("/{id}/teams/{teamId}")
    public ResponseEntity<GenericResponse> registerTeam(@PathVariable Long id, @PathVariable Long teamId) {
        log.info("POST /api/v1/tournaments/{}/teams/{}", id, teamId);
        try {
            tournamentService.registerTeamToTournament(id, teamId);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Equipo inscrito correctamente al torneo"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Generar partidos del torneo automáticamente",
            description = "Genera los partidos iniciales del torneo de forma aleatoria con los equipos inscritos.")
    @PostMapping("/{id}/generate-matches")
    public ResponseEntity<GenericResponse> generateMatches(@PathVariable Long id) {
        log.info("POST /api/v1/tournaments/{}/generate-matches", id);
        try {
            List<Match> matches = tournamentService.generateMatches(id);
            return ResponseEntity.ok(new GenericResponse("Éxito", matches.size() + " partidos generados correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Actualizar el estado del torneo")
    @PatchMapping("/{id}/status")
    public ResponseEntity<GenericResponse> updateTournamentStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        log.info("PATCH /api/v1/tournaments/{}/status", id);
        try {
            String newStatus = payload.get("status");
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El campo 'status' es obligatorio"));
            }
            tournamentService.updateTournamentStatus(id, newStatus);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Estado del torneo actualizado a: " + newStatus));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }
}