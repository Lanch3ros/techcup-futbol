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


    @Operation(summary = "Registrar un nuevo partido",
            description = "El organizador crea un partido definiendo los equipos, fecha y cancha.")
    @PostMapping
    public ResponseEntity<GenericResponse> createMatch(@RequestBody @Valid MatchCreationRequest request) {
        log.info("POST /api/v1/matches");
        try {
            Match match = matchService.createMatch(request);
            return new ResponseEntity<>(new GenericResponse("Éxito", match), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Listar todos los partidos",
            description = "Retorna todos los partidos registrados en el sistema.")
    @GetMapping
    public ResponseEntity<List<Match>> getAllMatches() {
        log.info("GET /api/v1/matches");
        return ResponseEntity.ok(matchService.getAllMatches());
    }


    @Operation(summary = "Consultar detalle de un partido",
            description = "Retorna la información completa de un partido: equipos, marcador, árbitro y estado.")
    @GetMapping("/{id}")
    public ResponseEntity<Match> getMatchById(@PathVariable Long id) {
        log.info("GET /api/v1/matches/{}", id);
        try {
            return ResponseEntity.ok(matchService.getMatchById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(summary = "Registrar resultado (marcador) de un partido",
            description = "El organizador ingresa los goles de cada equipo. El partido pasa a estado 'Finalizado'.")
    @PatchMapping("/{id}/result")
    public ResponseEntity<GenericResponse> registerResult(
            @PathVariable Long id,
            @RequestBody @Valid MatchResultRequest request) {

        log.info("PATCH /api/v1/matches/{}/result", id);
        try {
            matchService.registerResult(id, request);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Resultado registrado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Registrar evento en un partido",
            description = "Registra goles, tarjetas amarillas o tarjetas rojas durante un partido.")
    @PostMapping("/{id}/events")
    public ResponseEntity<GenericResponse> registerEvent(
            @PathVariable Long id,
            @RequestBody @Valid MatchEventRequest request) {

        log.info("POST /api/v1/matches/{}/events - tipo: {}", id, request.getType());
        try {
            MatchEvent event = matchService.registerEvent(id, request);
            return new ResponseEntity<>(new GenericResponse("Éxito", event), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Ver eventos de un partido",
            description = "Retorna todos los eventos registrados en el partido: goles, amarillas y rojas.")
    @GetMapping("/{id}/events")
    public ResponseEntity<List<MatchEvent>> getMatchEvents(@PathVariable Long id) {
        log.info("GET /api/v1/matches/{}/events", id);
        try {
            return ResponseEntity.ok(matchService.getMatchEvents(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @Operation(summary = "Ver alineaciones de ambos equipos en el partido",
            description = "Retorna las alineaciones configuradas por los capitanes para el partido.")
    @GetMapping("/{id}/lineups")
    public ResponseEntity<GenericResponse> getMatchLineups(@PathVariable Long id) {
        log.info("GET /api/v1/matches/{}/lineups", id);
        try {
            Match match = matchService.getMatchById(id);
            return ResponseEntity.ok(new GenericResponse("Alineaciones", match.getLineups()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Asignar árbitro a un partido",
            description = "Vincula un árbitro registrado a un partido específico.")
    @PatchMapping("/{id}/referee")
    public ResponseEntity<GenericResponse> assignReferee(
            @PathVariable Long id,
            @RequestBody Map<String, Long> payload) {

        log.info("PATCH /api/v1/matches/{}/referee", id);
        try {
            Long refereeId = payload.get("refereeId");
            if (refereeId == null) {
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El campo 'refereeId' es obligatorio"));
            }
            matchService.assignReferee(id, refereeId);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Árbitro asignado correctamente al partido"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }
}