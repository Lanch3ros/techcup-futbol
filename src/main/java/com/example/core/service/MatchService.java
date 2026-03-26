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
        log.info("Creando partido entre equipo {} y equipo {}", request.getHomeTeamId(), request.getAwayTeamId());

        Team homeTeam = teamRepository.findById(request.getHomeTeamId());
        Team awayTeam = teamRepository.findById(request.getAwayTeamId());

        if (homeTeam == null) throw new ResourceNotFoundException("Equipo local con ID " + request.getHomeTeamId() + " no encontrado");
        if (awayTeam == null) throw new ResourceNotFoundException("Equipo visitante con ID " + request.getAwayTeamId() + " no encontrado");
        if (request.getHomeTeamId().equals(request.getAwayTeamId())) throw new BusinessRuleException("Un equipo no puede jugar contra sí mismo");

        Match match = new Match();
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        match.setMatchDate(request.getMatchDate());
        match.setField(request.getField());
        match.setStatus("Programado");
        match.setEvents(new ArrayList<>());
        match.setLineups(new ArrayList<>());

        return matchRepository.save(match);
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public Match getMatchById(Long id) {
        Match match = matchRepository.findById(id);
        if (match == null) throw new ResourceNotFoundException("Partido con ID " + id + " no encontrado");
        return match;
    }

    public void registerResult(Long matchId, MatchResultRequest request) {
        log.info("Registrando resultado del partido {}: {} - {}", matchId, request.getHomeGoals(), request.getAwayGoals());
        Match match = getMatchById(matchId);
        match.setHomeGoals(request.getHomeGoals());
        match.setAwayGoals(request.getAwayGoals());
        match.setStatus("Finalizado");
        matchRepository.save(match);
    }

    public MatchEvent registerEvent(Long matchId, MatchEventRequest request) {
        log.info("Registrando evento tipo {} en el partido {}", request.getType(), matchId);
        getMatchById(matchId);

        MatchEvent event = new MatchEvent();
        event.setMatchId(matchId);
        event.setPlayerId(request.getPlayerId());
        event.setPlayerName(request.getPlayerName());
        event.setType(request.getType());
        event.setMinute(request.getMinute());

        return matchEventRepository.save(event);
    }

    public List<MatchEvent> getMatchEvents(Long matchId) {
        getMatchById(matchId);
        return matchEventRepository.findByMatchId(matchId);
    }

    public void assignReferee(Long matchId, Long refereeId) {
        log.info("Asignando árbitro {} al partido {}", refereeId, matchId);
        Match match = getMatchById(matchId);
        Referee referee = refereeRepository.findById(refereeId);
        if (referee == null) throw new ResourceNotFoundException("Árbitro con ID " + refereeId + " no encontrado");

        match.setReferee(referee.getFullName());
        referee.getAssignedMatchIds().add(matchId);

        matchRepository.save(match);
        refereeRepository.save(referee);
    }
}