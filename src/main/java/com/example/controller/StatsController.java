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


    @Operation(summary = "Máximos goleadores (global)")
    @GetMapping("/top-scorers")
    public ResponseEntity<List<PlayerStats>> getTopScorers() {
        log.info("GET /api/v1/stats/top-scorers");
        List<PlayerStats> scorers = statsService.getTopScorers();
        log.info("Tabla de goleadores globales retornada: {} jugadores", scorers.size());
        return ResponseEntity.ok(scorers);
    }


    @Operation(summary = "Máximos goleadores de un torneo")
    @GetMapping("/tournaments/{tournamentId}/top-scorers")
    public ResponseEntity<List<PlayerStats>> getTopScorersByTournament(@PathVariable Long tournamentId) {
        log.info("GET /api/v1/stats/tournaments/{}/top-scorers", tournamentId);
        List<PlayerStats> scorers = statsService.getTopScorersByTournament(tournamentId);
        log.info("Goleadores del torneo ID {}: {} jugadores", tournamentId, scorers.size());
        return ResponseEntity.ok(scorers);
    }


    @Operation(summary = "Estadísticas individuales de un jugador")
    @GetMapping("/players/{id}")
    public ResponseEntity<PlayerStats> getPlayerStats(@PathVariable Long id) {
        log.info("GET /api/v1/stats/players/{}", id);
        try {
            PlayerStats stats = statsService.getPlayerStats(id);
            log.info("Estadísticas del jugador ID {}: {} goles, {} amarillas, {} rojas",
                    id, stats.getGoals(), stats.getYellowCards(), stats.getRedCards());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.warn("Jugador no encontrado para estadísticas - ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(summary = "Estadísticas de un equipo")
    @GetMapping("/teams/{id}")
    public ResponseEntity<StandingDTO> getTeamStats(@PathVariable Long id) {
        log.info("GET /api/v1/stats/teams/{}", id);
        try {
            StandingDTO stats = statsService.getTeamStats(id);
            log.info("Estadísticas del equipo ID {}: {} pts, {} PJ, {} PG, {} PE, {} PP",
                    id, stats.getPoints(), stats.getMatchesPlayed(),
                    stats.getMatchesWon(), stats.getMatchesDrawn(), stats.getMatchesLost());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.warn("Equipo no encontrado para estadísticas - ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}