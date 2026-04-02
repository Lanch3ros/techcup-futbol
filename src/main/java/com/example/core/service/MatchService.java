package com.example.core.service;

import com.example.controller.dto.request.MatchCreationRequest;
import com.example.controller.dto.request.MatchEventRequest;
import com.example.controller.dto.request.MatchResultRequest;
import com.example.core.exception.BusinessRuleException;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.*;
import com.example.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;
    private final TeamRepository teamRepository;
    private final RefereeRepository refereeRepository;

    public MatchService(MatchRepository matchRepository,
                        MatchEventRepository matchEventRepository,
                        TeamRepository teamRepository,
                        RefereeRepository refereeRepository) {
        this.matchRepository = matchRepository;
        this.matchEventRepository = matchEventRepository;
        this.teamRepository = teamRepository;
        this.refereeRepository = refereeRepository;
    }

    public Match createMatch(MatchCreationRequest request) {
        log.info("Creando partido entre equipo local ID: {} y equipo visitante ID: {}", request.getHomeTeamId(), request.getAwayTeamId());

        Team homeTeam = teamRepository.findById(request.getHomeTeamId()).orElseThrow(() -> {
            log.warn("Equipo local no encontrado - ID: {}", request.getHomeTeamId());
            return new ResourceNotFoundException("Equipo local con ID " + request.getHomeTeamId() + " no encontrado");
        });
        Team awayTeam = teamRepository.findById(request.getAwayTeamId()).orElseThrow(() -> {
            log.warn("Equipo visitante no encontrado - ID: {}", request.getAwayTeamId());
            return new ResourceNotFoundException("Equipo visitante con ID " + request.getAwayTeamId() + " no encontrado");
        });
        if (request.getHomeTeamId().equals(request.getAwayTeamId())) {
            log.warn("Intento de crear partido con el mismo equipo como local y visitante - ID: {}", request.getHomeTeamId());
            throw new BusinessRuleException("Un equipo no puede jugar contra sí mismo");
        }

        Match match = new Match();
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        match.setMatchDate(request.getMatchDate());
        match.setField(request.getField());
        match.setStatus("Programado");
        match.setEvents(new ArrayList<>());
        match.setLineups(new ArrayList<>());

        Match saved = matchRepository.save(match);
        log.info("Partido creado exitosamente - ID: {}, estado: {}", saved.getId(), saved.getStatus());
        return saved;
    }

    public List<Match> getAllMatches() {
        log.info("Consultando la lista de todos los partidos");
        List<Match> matches = matchRepository.findAll();
        log.info("Total de partidos obtenidos: {}", matches.size());
        return matches;
    }

    public Match getMatchById(Long id) {
        log.info("Buscando partido con ID: {}", id);
        Match match = matchRepository.findById(id).orElseThrow(() -> {
            log.warn("Partido no encontrado - ID: {}", id);
            return new ResourceNotFoundException("Partido con ID " + id + " no encontrado");
        });
        log.info("Partido encontrado - ID: {}, estado: {}", id, match.getStatus());
        return match;
    }

    public void updateMatchStatus(Long matchId, String newStatus) {
        log.info("Actualizando estado del partido ID: {} a '{}'", matchId, newStatus);
        Match match = getMatchById(matchId);

        List<String> validStatuses = List.of("Programado", "En Curso", "Finalizado");
        if (!validStatuses.contains(newStatus)) {
            log.warn("Estado inválido '{}' para partido ID: {}", newStatus, matchId);
            throw new BusinessRuleException("Estado inválido. Los estados permitidos son: " + validStatuses);
        }

        match.setStatus(newStatus);
        matchRepository.save(match);
        log.info("Estado del partido ID: {} actualizado a '{}'", matchId, newStatus);
    }

    public void registerResult(Long matchId, MatchResultRequest request) {
        log.info("Registrando resultado del partido ID: {} - marcador: {} - {}", matchId, request.getHomeGoals(), request.getAwayGoals());
        Match match = getMatchById(matchId);

        if (!"Finalizado".equalsIgnoreCase(match.getStatus())) {
            log.warn("Intento de registrar resultado en partido no finalizado - ID: {}, estado actual: '{}'", matchId, match.getStatus());
            throw new BusinessRuleException("El resultado solo puede registrarse una vez que el partido está en estado 'Finalizado'.");
        }

        match.setHomeGoals(request.getHomeGoals());
        match.setAwayGoals(request.getAwayGoals());
        matchRepository.save(match);
        log.info("Resultado registrado exitosamente para partido ID: {} -> {} - {}", matchId, request.getHomeGoals(), request.getAwayGoals());
    }

    public MatchEvent registerEvent(Long matchId, MatchEventRequest request) {
        log.info("Registrando evento tipo '{}' en partido ID: {}, jugador ID: {}, minuto: {}", request.getType(), matchId, request.getPlayerId(), request.getMinute());
        getMatchById(matchId);

        MatchEvent event = new MatchEvent();
        event.setMatchId(matchId);
        event.setPlayerId(request.getPlayerId());
        event.setPlayerName(request.getPlayerName());
        event.setType(request.getType());
        event.setMinute(request.getMinute());

        MatchEvent saved = matchEventRepository.save(event);
        log.info("Evento '{}' registrado exitosamente en partido ID: {} - ID evento: {}", request.getType(), matchId, saved.getId());
        return saved;
    }

    public List<MatchEvent> getMatchEvents(Long matchId) {
        log.info("Consultando eventos del partido ID: {}", matchId);
        getMatchById(matchId);
        List<MatchEvent> events = matchEventRepository.findByMatchId(matchId);
        log.info("Total de eventos encontrados para partido ID {}: {}", matchId, events.size());
        return events;
    }

    public void assignReferee(Long matchId, Long refereeId) {
        log.info("Asignando árbitro ID: {} al partido ID: {}", refereeId, matchId);
        Match match = getMatchById(matchId);

        Referee referee = refereeRepository.findById(refereeId).orElseThrow(() -> {
            log.warn("Árbitro no encontrado - ID: {}", refereeId);
            return new ResourceNotFoundException("Árbitro con ID " + refereeId + " no encontrado");
        });

        match.setReferee(referee.getFullName());
        referee.getAssignedMatchIds().add(matchId);

        matchRepository.save(match);
        refereeRepository.save(referee);
        log.info("Árbitro '{}' asignado exitosamente al partido ID: {}", referee.getFullName(), matchId);
    }
}