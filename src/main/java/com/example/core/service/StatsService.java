package com.example.core.service;

import com.example.controller.dto.response.StandingDTO;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.*;
import com.example.repository.MatchEventRepository;
import com.example.repository.MatchRepository;
import com.example.repository.TeamRepository;
import com.example.repository.UserRepository;
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
    private final UserRepository userRepository;

    public StatsService(MatchEventRepository matchEventRepository,
                        MatchRepository matchRepository,
                        TeamRepository teamRepository,
                        UserRepository userRepository) {
        this.matchEventRepository = matchEventRepository;
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
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
                .filter(e -> "GOL".equalsIgnoreCase(e.getType()))
                .collect(Collectors.toList());
        List<PlayerStats> scorers = buildPlayerStatsFromEvents(events);
        log.info("Goleadores del torneo ID {}: {} jugadores", tournamentId, scorers.size());
        return scorers;
    }

    public PlayerStats getPlayerStats(Long playerId) {
        log.info("Consultando estadísticas del jugador ID: {}", playerId);
        User player = userRepository.findById(playerId).orElseThrow(() -> {
            log.warn("Jugador no encontrado al consultar estadísticas - ID: {}", playerId);
            return new ResourceNotFoundException("Jugador con ID " + playerId + " no encontrado");
        });

        List<MatchEvent> playerEvents = matchEventRepository.findAll().stream()
                .filter(e -> e.getPlayerId().equals(playerId))
                .collect(Collectors.toList());

        PlayerStats stats = new PlayerStats();
        stats.setPlayerId(playerId);
        stats.setPlayerName(player.getFullName());
        stats.setGoals((int) playerEvents.stream().filter(e -> "GOL".equalsIgnoreCase(e.getType())).count());
        stats.setYellowCards((int) playerEvents.stream().filter(e -> "AMARILLA".equalsIgnoreCase(e.getType())).count());
        stats.setRedCards((int) playerEvents.stream().filter(e -> "ROJA".equalsIgnoreCase(e.getType())).count());

        log.info("Estadísticas del jugador ID {}: {} goles, {} amarillas, {} rojas", playerId, stats.getGoals(), stats.getYellowCards(), stats.getRedCards());
        return stats;
    }

    public StandingDTO getTeamStats(Long teamId) {
        log.info("Consultando estadísticas del equipo ID: {}", teamId);
        Team team = teamRepository.findById(teamId).orElseThrow(() -> {
            log.warn("Equipo no encontrado al consultar estadísticas - ID: {}", teamId);
            return new ResourceNotFoundException("Equipo con ID " + teamId + " no encontrado");
        });

        int fairPlayPoints = calculateFairPlayPoints(teamId);
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
        dto.setPoints(team.getPoints() + fairPlayPoints);

        log.info("Estadísticas del equipo ID {}: {} pts (incl. {} FairPlay), {} PJ", teamId, dto.getPoints(), fairPlayPoints, dto.getMatchesPlayed());
        return dto;
    }

    public List<StandingDTO> getTournamentStandings(Long tournamentId) {
        log.info("Calculando tabla de posiciones del torneo ID: {}", tournamentId);
        List<StandingDTO> standings = teamRepository.findAll().stream()
                .map(team -> {
                    int fairPlayPoints = calculateFairPlayPoints(team.getId());
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
                    dto.setPoints(team.getPoints() + fairPlayPoints);
                    return dto;
                })
                .sorted(Comparator.comparingInt(StandingDTO::getPoints).reversed())
                .collect(Collectors.toList());
        log.info("Tabla de posiciones calculada para torneo ID {}: {} equipos", tournamentId, standings.size());
        return standings;
    }

    // RN-09-2: +1 punto por cada partido finalizado en que el equipo no recibió tarjetas
    private int calculateFairPlayPoints(Long teamId) {
        return (int) matchRepository.findAll().stream()
                .filter(m -> "Finalizado".equalsIgnoreCase(m.getStatus()))
                .filter(m -> teamParticipatedInMatch(m, teamId))
                .filter(m -> teamHadNoCardsInMatch(m, teamId))
                .count();
    }

    private boolean teamParticipatedInMatch(Match match, Long teamId) {
        return (match.getHomeTeam() != null && teamId.equals(match.getHomeTeam().getId()))
                || (match.getAwayTeam() != null && teamId.equals(match.getAwayTeam().getId()));
    }

    private boolean teamHadNoCardsInMatch(Match match, Long teamId) {
        return matchEventRepository.findByMatchId(match.getId()).stream()
                .filter(e -> "AMARILLA".equalsIgnoreCase(e.getType()) || "ROJA".equalsIgnoreCase(e.getType()))
                .noneMatch(e -> {
                    User player = userRepository.findById(e.getPlayerId()).orElse(null);
                    return player != null && teamId.equals(player.getTeamId());
                });
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