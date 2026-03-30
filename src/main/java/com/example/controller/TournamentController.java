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
        log.info("POST /api/v1/tournaments - inicio: {}, fin: {}, equipos: {}", request.getStartDate(), request.getEndDate(), request.getNumberOfTeams());
        try {
            Tournament tournamentEntity = tournamentMapper.toEntity(request);
            tournamentService.createTournament(tournamentEntity);
            log.info("Torneo creado exitosamente en estado Borrador");
            return new ResponseEntity<>(new GenericResponse("Éxito", "Torneo creado correctamente en estado Borrador"), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error al crear torneo: {}", e.getMessage());
            return new ResponseEntity<>(new GenericResponse("Error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }


    @Operation(summary = "Listar todos los torneos")
    @GetMapping
    public ResponseEntity<List<Tournament>> getAllTournaments() {
        log.info("GET /api/v1/tournaments");
        List<Tournament> tournaments = tournamentService.getAllTournaments();
        log.info("Total de torneos retornados: {}", tournaments.size());
        return ResponseEntity.ok(tournaments);
    }


    @Operation(summary = "Consultar un torneo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Tournament> getTournamentById(@PathVariable Long id) {
        log.info("GET /api/v1/tournaments/{}", id);
        try {
            Tournament tournament = tournamentService.getTournamentById(id);
            log.info("Torneo encontrado - ID: {}, estado: {}", id, tournament.getStatus());
            return ResponseEntity.ok(tournament);
        } catch (Exception e) {
            log.warn("Torneo no encontrado - ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(summary = "Consultar el reglamento del torneo")
    @GetMapping("/{id}/rules")
    public ResponseEntity<GenericResponse> getTournamentRules(@PathVariable Long id) {
        log.info("GET /api/v1/tournaments/{}/rules", id);
        try {
            Tournament tournament = tournamentService.getTournamentById(id);
            log.info("Reglamento consultado para torneo ID: {}", id);
            return ResponseEntity.ok(new GenericResponse("Reglamento", tournament.getRegulations()));
        } catch (Exception e) {
            log.warn("No se encontró reglamento para torneo ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Ver equipos inscritos en el torneo")
    @GetMapping("/{id}/teams")
    public ResponseEntity<List<Team>> getTournamentTeams(@PathVariable Long id) {
        log.info("GET /api/v1/tournaments/{}/teams", id);
        try {
            List<Team> teams = tournamentService.getTournamentTeams(id);
            log.info("Equipos inscritos en torneo ID {}: {}", id, teams.size());
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            log.error("Error al obtener equipos del torneo ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }


    @Operation(summary = "Tabla de posiciones del torneo")
    @GetMapping("/{id}/standings")
    public ResponseEntity<List<StandingDTO>> getTournamentStandings(@PathVariable Long id) {
        log.info("GET /api/v1/tournaments/{}/standings", id);
        try {
            List<StandingDTO> standings = statsService.getTournamentStandings(id);
            log.info("Tabla de posiciones calculada para torneo ID: {} - {} equipos", id, standings.size());
            return ResponseEntity.ok(standings);
        } catch (Exception e) {
            log.error("Error al calcular tabla de posiciones del torneo ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }


    @Operation(summary = "Llaves eliminatorias del torneo")
    @GetMapping("/{id}/bracket")
    public ResponseEntity<GenericResponse> getTournamentBracket(@PathVariable Long id) {
        log.info("GET /api/v1/tournaments/{}/bracket", id);
        try {
            List<Match> matches = tournamentService.getTournamentById(id).getMatches();
            if (matches == null || matches.isEmpty()) {
                log.warn("No se han generado partidos para el torneo ID: {}", id);
                return ResponseEntity.ok(new GenericResponse("Info", "No se han generado partidos para este torneo todavía"));
            }
            log.info("Llaves retornadas para torneo ID: {} - {} partidos", id, matches.size());
            return ResponseEntity.ok(new GenericResponse("Llaves", matches));
        } catch (Exception e) {
            log.error("Error al obtener llaves del torneo ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Inscribir equipo al torneo")
    @PostMapping("/{id}/teams/{teamId}")
    public ResponseEntity<GenericResponse> registerTeam(@PathVariable Long id, @PathVariable Long teamId) {
        log.info("POST /api/v1/tournaments/{}/teams/{}", id, teamId);
        try {
            tournamentService.registerTeamToTournament(id, teamId);
            log.info("Equipo ID: {} inscrito exitosamente en torneo ID: {}", teamId, id);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Equipo inscrito correctamente al torneo"));
        } catch (Exception e) {
            log.error("Error al inscribir equipo ID: {} en torneo ID: {} - {}", teamId, id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Generar partidos del torneo automáticamente")
    @PostMapping("/{id}/generate-matches")
    public ResponseEntity<GenericResponse> generateMatches(@PathVariable Long id) {
        log.info("POST /api/v1/tournaments/{}/generate-matches", id);
        try {
            List<Match> matches = tournamentService.generateMatches(id);
            log.info("{} partidos generados exitosamente para el torneo ID: {}", matches.size(), id);
            return ResponseEntity.ok(new GenericResponse("Éxito", matches.size() + " partidos generados correctamente"));
        } catch (Exception e) {
            log.error("Error al generar partidos para el torneo ID: {} - {}", id, e.getMessage());
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
                log.warn("Campo 'status' vacío en la petición para torneo ID: {}", id);
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El campo 'status' es obligatorio"));
            }
            tournamentService.updateTournamentStatus(id, newStatus);
            log.info("Estado del torneo ID: {} actualizado a: {}", id, newStatus);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Estado del torneo actualizado a: " + newStatus));
        } catch (Exception e) {
            log.error("Error al actualizar estado del torneo ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }
}