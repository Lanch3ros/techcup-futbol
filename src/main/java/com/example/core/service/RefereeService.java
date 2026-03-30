package com.example.core.service;

import com.example.controller.dto.request.RefereeRequest;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.Match;
import com.example.core.model.Referee;
import com.example.repository.MatchRepository;
import com.example.repository.RefereeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RefereeService {

    private final RefereeRepository refereeRepository;
    private final MatchRepository matchRepository;

    public RefereeService(RefereeRepository refereeRepository, MatchRepository matchRepository) {
        this.refereeRepository = refereeRepository;
        this.matchRepository = matchRepository;
    }

    public Referee createReferee(RefereeRequest request) {
        log.info("Registrando árbitro - nombre: {}, licencia: {}", request.getFullName(), request.getLicenseNumber());
        Referee referee = new Referee();
        referee.setFullName(request.getFullName());
        referee.setEmail(request.getEmail());
        referee.setLicenseNumber(request.getLicenseNumber());
        referee.setCertificationLevel(request.getCertificationLevel());
        Referee saved = refereeRepository.save(referee);
        log.info("Árbitro registrado exitosamente - ID: {}, nombre: '{}'", saved.getId(), saved.getFullName());
        return saved;
    }

    public List<Referee> getAllReferees() {
        log.info("Consultando la lista de todos los árbitros");
        List<Referee> referees = refereeRepository.findAll();
        log.info("Total de árbitros obtenidos: {}", referees.size());
        return referees;
    }

    public Referee getRefereeById(Long id) {
        log.info("Buscando árbitro con ID: {}", id);
        Referee referee = refereeRepository.findById(id);
        if (referee == null) {
            log.warn("Árbitro no encontrado - ID: {}", id);
            throw new ResourceNotFoundException("Árbitro con ID " + id + " no encontrado");
        }
        log.info("Árbitro encontrado - ID: {}, nombre: '{}'", id, referee.getFullName());
        return referee;
    }

    public List<Match> getRefereeMatches(Long id) {
        log.info("Consultando partidos asignados al árbitro ID: {}", id);
        Referee referee = getRefereeById(id);
        List<Match> matches = referee.getAssignedMatchIds().stream()
                .map(matchId -> matchRepository.findById(matchId))
                .filter(m -> m != null)
                .collect(Collectors.toList());
        log.info("Total de partidos asignados al árbitro ID {}: {}", id, matches.size());
        return matches;
    }
}