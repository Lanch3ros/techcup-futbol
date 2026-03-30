package com.example.core.service;

import com.example.controller.dto.response.StandingDTO;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.*;
import com.example.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatsService {

    private final MatchEventRepository matchEventRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    public StatsService(MatchEventRepository matchEventRepository,
                        MatchRepository matchRepository,
                        TeamRepository teamRepository,
                        PlayerRepository playerRepository) {
        this.matchEventRepository = matchEventRepository;
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }

    public List<PlayerStats> getTopScorers() {
        log.info("Consultando tabla de goleadores globales");
        List<PlayerStats> scorers = buildPlayerStatsFromEvents(matchEventRepository.findAll());
        log.info("Tabla de goleadores globales calculada: {} jugadores", scorers.size());
        return scorers;
    }

    public List<PlayerStats> getTopScorersByTournament(Long tournamentId) {
        log.info("Consultando goleadores del torneo ID: {}", tournamentId);
        List<MatchEvent> events = matchEventRepository.findAll().stream()
                .filter(e -> "GOL".equals(e.getType()))
                .collect(Collectors.toList());
        List<PlayerStats> scorers = buildPlayerStatsFromEvents(events);
        log.info("Goleadores del torneo ID {}: {} jugadores", tournamentId, scorers.size());
        return scorers;
    }

    public PlayerStats getPlayerStats(Long playerId) {
        log.info("Consultando estadísticas del jugador ID: {}", playerId);
        Player player = playerRepository.findById(playerId);
        if (player == null) {
            log.warn("Jugador no encontrado al consultar estadísticas - ID: {}", playerId);
            throw new ResourceNotFoundException("Jugador con ID " + playerId + " no encontrado");
        }

        List<MatchEvent> playerEvents = matchEventRepository.findAll().stream()
                .filter(e -> e.getPlayerId().equals(playerId))
                .collect(Collectors.toList());

        PlayerStats stats = new PlayerStats();
        stats.setPlayerId(playerId);
        stats.setPlayerName(player.getFullName());
        stats.setGoals((int) playerEvents.stream().filter(e -> "GOL".equals(e.getType())).count());
        stats.setYellowCards((int) playerEvents.stream().filter(e -> "AMARILLA".equals(e.getType())).count());
        stats.setRedCards((int) playerEvents.stream().filter(e -> "ROJA".equals(e.getType())).count());

        log.info("Estadísticas del jugador ID {}: {} goles, {} amarillas, {} rojas", playerId, stats.getGoals(), stats.getYellowCards(), stats.getRedCards());
        return stats;
    }

    public StandingDTO getTeamStats(Long teamId) {
        log.info("Consultando estadísticas del equipo ID: {}", teamId);
        Team team = teamRepository.findById(teamId);
        if (team == null) {
            log.warn("Equipo no encontrado al consultar estadísticas - ID: {}", teamId);
            throw new ResourceNotFoundException("Equipo con ID " + teamId + " no encontrado");
        }

        StandingDTO dto = new StandingDTO();
        dto.setTeamId(teamId);
        dto.setTeamName(team.getName());
        dto.setMatchesPlayed(team.getMatchesPlayed());
        dto.setMatchesWon(team.getMatchesWon());
        dto.setMatchesDrawn(team.getMatchesDrawn());
        dto.setMatchesLost(team.getMatchesLost());
        dto.setGoalsFor(team.getGoalsFor());
        dto.setGoalsAgainst(team.getGoalsAgainst());
        dto.setGoalDifference(team.getGoalDifference());
        dto.setPoints(team.getPoints());

        log.info("Estadísticas del equipo ID {}: {} pts, {} PJ, {} PG, {} PE, {} PP", teamId, dto.getPoints(), dto.getMatchesPlayed(), dto.getMatchesWon(), dto.getMatchesDrawn(), dto.getMatchesLost());
        return dto;
    }

    public List<StandingDTO> getTournamentStandings(Long tournamentId) {
        log.info("Calculando tabla de posiciones del torneo ID: {}", tournamentId);
        List<StandingDTO> standings = teamRepository.findAll().stream()
                .map(team -> {
                    StandingDTO dto = new StandingDTO();
                    dto.setTeamId(team.getId());
                    dto.setTeamName(team.getName());
                    dto.setMatchesPlayed(team.getMatchesPlayed());
                    dto.setMatchesWon(team.getMatchesWon());
                    dto.setMatchesDrawn(team.getMatchesDrawn());
                    dto.setMatchesLost(team.getMatchesLost());
                    dto.setGoalsFor(team.getGoalsFor());
                    dto.setGoalsAgainst(team.getGoalsAgainst());
                    dto.setGoalDifference(team.getGoalDifference());
                    dto.setPoints(team.getPoints());
                    return dto;
                })
                .sorted(Comparator.comparingInt(StandingDTO::getPoints).reversed())
                .collect(Collectors.toList());
        log.info("Tabla de posiciones calculada para torneo ID {}: {} equipos", tournamentId, standings.size());
        return standings;
    }

    private List<PlayerStats> buildPlayerStatsFromEvents(List<MatchEvent> events) {
        Map<Long, PlayerStats> statsMap = new HashMap<>();

        for (MatchEvent event : events) {
            statsMap.putIfAbsent(event.getPlayerId(), new PlayerStats(
                    event.getPlayerId(), event.getPlayerName(), "", 0, 0, 0, 0));

            PlayerStats stats = statsMap.get(event.getPlayerId());
            switch (event.getType()) {
                case "GOL" -> stats.setGoals(stats.getGoals() + 1);
                case "AMARILLA" -> stats.setYellowCards(stats.getYellowCards() + 1);
                case "ROJA" -> stats.setRedCards(stats.getRedCards() + 1);
            }
        }

        return statsMap.values().stream()
                .sorted(Comparator.comparingInt(PlayerStats::getGoals).reversed())
                .collect(Collectors.toList());
    }
}