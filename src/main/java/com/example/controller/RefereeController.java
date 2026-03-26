package com.example.controller;

import com.example.controller.dto.request.RefereeRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.core.model.Match;
import com.example.core.model.Referee;
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


    @Operation(summary = "Registrar un nuevo árbitro",
            description = "Crea un árbitro con su información personal, licencia y nivel de certificación.")
    @PostMapping
    public ResponseEntity<GenericResponse> createReferee(@RequestBody @Valid RefereeRequest request) {
        log.info("POST /api/v1/referees");
        try {
            Referee referee = refereeService.createReferee(request);
            return new ResponseEntity<>(new GenericResponse("Éxito", referee), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Listar todos los árbitros",
            description = "Retorna todos los árbitros registrados en el sistema.")
    @GetMapping
    public ResponseEntity<List<Referee>> getAllReferees() {
        log.info("GET /api/v1/referees");
        return ResponseEntity.ok(refereeService.getAllReferees());
    }


    @Operation(summary = "Ver perfil de un árbitro",
            description = "Retorna la información detallada de un árbitro específico.")
    @GetMapping("/{id}")
    public ResponseEntity<Referee> getRefereeById(@PathVariable Long id) {
        log.info("GET /api/v1/referees/{}", id);
        try {
            return ResponseEntity.ok(refereeService.getRefereeById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(summary = "Ver partidos asignados al árbitro",
            description = "El árbitro puede consultar todos los partidos que tiene programados para arbitrar.")
    @GetMapping("/{id}/matches")
    public ResponseEntity<List<Match>> getRefereeMatches(@PathVariable Long id) {
        log.info("GET /api/v1/referees/{}/matches", id);
        try {
            return ResponseEntity.ok(refereeService.getRefereeMatches(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}