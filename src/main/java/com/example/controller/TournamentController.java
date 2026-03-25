package com.example.controller;

import com.example.controller.dto.request.TournamentCreationRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.controller.mapper.TournamentMapper;
import com.example.core.model.Tournament;
import com.example.core.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/tournaments")
@Tag(name = "Torneos", description = "Endpoints para la gestión y configuración principal del torneo")
public class TournamentController {

    private final TournamentService tournamentService;
    private final TournamentMapper tournamentMapper;

    public TournamentController(TournamentService tournamentService, TournamentMapper tournamentMapper) {
        this.tournamentService = tournamentService;
        this.tournamentMapper = tournamentMapper;
    }

    @Operation(summary = "Registrar y configurar un nuevo torneo", description = "Crea un torneo definiendo sus fechas, costo de inscripción, reglamento y cantidad de equipos. El estado inicial siempre será 'Borrador'.")
    @PostMapping
    public ResponseEntity<GenericResponse> createTournament(@RequestBody @Valid TournamentCreationRequest request) {
        log.info("Petición REST POST recibida en /api/v1/tournaments para crear un torneo.");

        try {
            Tournament tournamentEntity = tournamentMapper.toEntity(request);
            tournamentService.createTournament(tournamentEntity);

            log.info("Torneo procesado y creado exitosamente. Retornando código HTTP 201 (CREATED).");
            return new ResponseEntity<>(new GenericResponse("Éxito", "Torneo creado correctamente en estado Borrador"), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error al crear el torneo: {}", e.getMessage(), e);
            return new ResponseEntity<>(new GenericResponse("Error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Consultar el reglamento oficial del torneo", description = "Obtiene el texto del reglamento y reglas configuradas por el organizador para un torneo específico.")
    @GetMapping("/{id}/rules")
    public ResponseEntity<GenericResponse> getTournamentRules(@PathVariable Long id) {
        log.info("Petición REST GET recibida en /api/v1/tournaments/{}/rules", id);

        try {
            Tournament tournament = tournamentService.getTournamentById(id);
            String rules = tournament.getRegulations();

            log.info("Reglamento del torneo {} consultado exitosamente.", id);
            return ResponseEntity.ok(new GenericResponse("Reglamento", rules));
        } catch (Exception e) {
            log.error("Error al consultar el reglamento del torneo {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GenericResponse("Error", e.getMessage()));
        }
    }

    @Operation(summary = "Actualizar el estado actual del torneo", description = "Permite al organizador avanzar el ciclo de vida del torneo (Borrador -> Activo -> En progreso -> Finalizado).")
    @PatchMapping("/{id}/status")
    public ResponseEntity<GenericResponse> updateTournamentStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        log.info("Petición REST PATCH recibida en /api/v1/tournaments/{}/status", id);

        try {
            String newStatus = payload.get("status");
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El campo 'status' es obligatorio"));
            }

            tournamentService.updateTournamentStatus(id, newStatus);

            log.info("Estado del torneo {} actualizado a {}.", id, newStatus);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Estado del torneo actualizado a: " + newStatus));
        } catch (Exception e) {
            log.error("Error al actualizar el estado del torneo {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }
}