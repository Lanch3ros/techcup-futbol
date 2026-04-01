package com.example.controller;

import com.example.controller.dto.request.MatchCreationRequest;
import com.example.controller.dto.request.MatchEventRequest;
import com.example.controller.dto.request.MatchResultRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.core.model.Match;
import com.example.core.model.MatchEvent;
import com.example.core.service.MatchService;
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
@RequestMapping("/api/v1/matches")
@Tag(name = "Partidos", description = "Endpoints para el registro y consulta de partidos del torneo")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }


    @Operation(summary = "Registrar un nuevo partido")
    @PostMapping
    public ResponseEntity<GenericResponse> createMatch(@RequestBody @Valid MatchCreationRequest request) {
        log.info("POST /api/v1/matches - equipo local ID: {}, equipo visitante ID: {}, fecha: {}",
                request.getHomeTeamId(), request.getAwayTeamId(), request.getMatchDate());
        try {
            Match match = matchService.createMatch(request);
            log.info("Partido creado exitosamente - ID: {}", match.getId());
            return new ResponseEntity<>(new GenericResponse("Éxito", match), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error al crear partido entre equipos {} y {}: {}", request.getHomeTeamId(), request.getAwayTeamId(), e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Listar todos los partidos")
    @GetMapping
    public ResponseEntity<List<Match>> getAllMatches() {
        log.info("GET /api/v1/matches");
        List<Match> matches = matchService.getAllMatches();
        log.info("Total de partidos retornados: {}", matches.size());
        return ResponseEntity.ok(matches);
    }


    @Operation(summary = "Consultar detalle de un partido")
    @GetMapping("/{id}")
    public ResponseEntity<Match> getMatchById(@PathVariable Long id) {
        log.info("GET /api/v1/matches/{}", id);
        try {
            Match match = matchService.getMatchById(id);
            log.info("Partido encontrado - ID: {}, estado: {}", id, match.getStatus());
            return ResponseEntity.ok(match);
        } catch (Exception e) {
            log.warn("Partido no encontrado - ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(summary = "Actualizar estado de un partido (Programado → En Curso → Finalizado)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<GenericResponse> updateMatchStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        log.info("PATCH /api/v1/matches/{}/status", id);
        try {
            String newStatus = payload.get("status");
            if (newStatus == null || newStatus.isBlank()) {
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El campo 'status' es obligatorio"));
            }
            matchService.updateMatchStatus(id, newStatus);
            log.info("Estado del partido ID: {} actualizado a '{}'", id, newStatus);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Estado actualizado a '" + newStatus + "'"));
        } catch (Exception e) {
            log.error("Error al actualizar estado del partido ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Registrar resultado (marcador) de un partido")
    @PatchMapping("/{id}/result")
    public ResponseEntity<GenericResponse> registerResult(
            @PathVariable Long id,
            @RequestBody @Valid MatchResultRequest request) {

        log.info("PATCH /api/v1/matches/{}/result - marcador: {} - {}", id, request.getHomeGoals(), request.getAwayGoals());
        try {
            matchService.registerResult(id, request);
            log.info("Resultado registrado para partido ID: {} -> {} - {}", id, request.getHomeGoals(), request.getAwayGoals());
            return ResponseEntity.ok(new GenericResponse("Éxito", "Resultado registrado correctamente"));
        } catch (Exception e) {
            log.error("Error al registrar resultado del partido ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Registrar evento en un partido")
    @PostMapping("/{id}/events")
    public ResponseEntity<GenericResponse> registerEvent(
            @PathVariable Long id,
            @RequestBody @Valid MatchEventRequest request) {

        log.info("POST /api/v1/matches/{}/events - tipo: {}, jugador ID: {}, minuto: {}",
                id, request.getType(), request.getPlayerId(), request.getMinute());
        try {
            MatchEvent event = matchService.registerEvent(id, request);
            log.info("Evento '{}' registrado en partido ID: {} en el minuto {}", request.getType(), id, request.getMinute());
            return new ResponseEntity<>(new GenericResponse("Éxito", event), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error al registrar evento en partido ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Ver eventos de un partido")
    @GetMapping("/{id}/events")
    public ResponseEntity<List<MatchEvent>> getMatchEvents(@PathVariable Long id) {
        log.info("GET /api/v1/matches/{}/events", id);
        try {
            List<MatchEvent> events = matchService.getMatchEvents(id);
            log.info("Eventos retornados para partido ID {}: {}", id, events.size());
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error al obtener eventos del partido ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }


    @Operation(summary = "Ver alineaciones de ambos equipos en el partido")
    @GetMapping("/{id}/lineups")
    public ResponseEntity<GenericResponse> getMatchLineups(@PathVariable Long id) {
        log.info("GET /api/v1/matches/{}/lineups", id);
        try {
            Match match = matchService.getMatchById(id);
            log.info("Alineaciones consultadas para partido ID: {}", id);
            return ResponseEntity.ok(new GenericResponse("Alineaciones", match.getLineups()));
        } catch (Exception e) {
            log.error("Error al obtener alineaciones del partido ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Asignar árbitro a un partido")
    @PatchMapping("/{id}/referee")
    public ResponseEntity<GenericResponse> assignReferee(
            @PathVariable Long id,
            @RequestBody Map<String, Long> payload) {

        log.info("PATCH /api/v1/matches/{}/referee", id);
        try {
            Long refereeId = payload.get("refereeId");
            if (refereeId == null) {
                log.warn("Campo 'refereeId' no proporcionado para partido ID: {}", id);
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El campo 'refereeId' es obligatorio"));
            }
            matchService.assignReferee(id, refereeId);
            log.info("Árbitro ID: {} asignado exitosamente al partido ID: {}", refereeId, id);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Árbitro asignado correctamente al partido"));
        } catch (Exception e) {
            log.error("Error al asignar árbitro al partido ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }
}