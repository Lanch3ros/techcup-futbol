package com.example.controller;

import com.example.controller.dto.response.GenericResponse;
import com.example.controller.dto.response.StandingDTO;
import com.example.core.model.PlayerStats;
import com.example.core.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/stats")
@Tag(name = "Estadísticas", description = "Endpoints para consultar estadísticas del torneo, equipos y jugadores")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }


    @Operation(summary = "Máximos goleadores (global)",
            description = "Retorna la tabla de goleadores ordenada de mayor a menor cantidad de goles.")
    @GetMapping("/top-scorers")
    public ResponseEntity<List<PlayerStats>> getTopScorers() {
        log.info("GET /api/v1/stats/top-scorers");
        return ResponseEntity.ok(statsService.getTopScorers());
    }


    @Operation(summary = "Máximos goleadores de un torneo",
            description = "Retorna los goleadores filtrados por torneo específico.")
    @GetMapping("/tournaments/{tournamentId}/top-scorers")
    public ResponseEntity<List<PlayerStats>> getTopScorersByTournament(@PathVariable Long tournamentId) {
        log.info("GET /api/v1/stats/tournaments/{}/top-scorers", tournamentId);
        return ResponseEntity.ok(statsService.getTopScorersByTournament(tournamentId));
    }


    @Operation(summary = "Estadísticas individuales de un jugador",
            description = "Retorna goles, tarjetas amarillas, tarjetas rojas y partidos jugados de un jugador.")
    @GetMapping("/players/{id}")
    public ResponseEntity<PlayerStats> getPlayerStats(@PathVariable Long id) {
        log.info("GET /api/v1/stats/players/{}", id);
        try {
            return ResponseEntity.ok(statsService.getPlayerStats(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(summary = "Estadísticas de un equipo",
            description = "Retorna partidos jugados, ganados, empatados, perdidos, goles y puntos del equipo.")
    @GetMapping("/teams/{id}")
    public ResponseEntity<StandingDTO> getTeamStats(@PathVariable Long id) {
        log.info("GET /api/v1/stats/teams/{}", id);
        try {
            return ResponseEntity.ok(statsService.getTeamStats(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}