package com.example.controller;

import com.example.controller.dto.request.RefereeRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.core.model.Match;
import com.example.core.model.RefereeUser;
import com.example.core.service.RefereeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/referees")
@Tag(name = "Árbitros", description = "Endpoints para el registro y consulta de árbitros del torneo")
public class RefereeController {

    private final RefereeService refereeService;

    public RefereeController(RefereeService refereeService) {
        this.refereeService = refereeService;
    }


    @Operation(summary = "Registrar un nuevo árbitro")
    @PostMapping
    public ResponseEntity<GenericResponse> createReferee(@RequestBody @Valid RefereeRequest request) {
        log.info("POST /api/v1/referees - nombre: {}, licencia: {}", request.getFullName(), request.getLicenseNumber());
        try {
            RefereeUser referee = refereeService.createReferee(request);
            log.info("Árbitro registrado exitosamente - nombre: {}", request.getFullName());
            return new ResponseEntity<>(new GenericResponse("Éxito", referee), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error al registrar árbitro '{}': {}", request.getFullName(), e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Listar todos los árbitros")
    @GetMapping
    public ResponseEntity<List<RefereeUser>> getAllReferees() {
        log.info("GET /api/v1/referees");
        List<RefereeUser> referees = refereeService.getAllReferees();
        log.info("Total de árbitros retornados: {}", referees.size());
        return ResponseEntity.ok(referees);
    }


    @Operation(summary = "Ver perfil de un árbitro")
    @GetMapping("/{id}")
    public ResponseEntity<RefereeUser> getRefereeById(@PathVariable Long id) {
        log.info("GET /api/v1/referees/{}", id);
        try {
            RefereeUser referee = refereeService.getRefereeById(id);
            log.info("Árbitro encontrado - ID: {}, nombre: {}", id, referee.getFullName());
            return ResponseEntity.ok(referee);
        } catch (Exception e) {
            log.warn("Árbitro no encontrado - ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(summary = "Ver partidos asignados al árbitro")
    @GetMapping("/{id}/matches")
    public ResponseEntity<List<Match>> getRefereeMatches(@PathVariable Long id) {
        log.info("GET /api/v1/referees/{}/matches", id);
        try {
            List<Match> matches = refereeService.getRefereeMatches(id);
            log.info("Partidos asignados al árbitro ID {}: {}", id, matches.size());
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            log.error("Error al obtener partidos del árbitro ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
