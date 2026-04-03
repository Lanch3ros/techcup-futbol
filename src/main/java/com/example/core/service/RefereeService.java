package com.example.core.service;

import com.example.controller.dto.request.RefereeRequest;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.Match;
import com.example.core.model.RefereeUser;
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

    public RefereeUser createReferee(RefereeRequest request) {
        log.info("Registrando árbitro - nombre: {}, licencia: {}", request.getFullName(), request.getLicenseNumber());
        RefereeUser referee = new RefereeUser();
        referee.setFullName(request.getFullName());
        referee.setEmail(request.getEmail());
        referee.setLicenseNumber(request.getLicenseNumber());
        referee.setCertificationLevel(request.getCertificationLevel());
        RefereeUser saved = refereeRepository.save(referee);
        log.info("Árbitro registrado exitosamente - ID: {}, nombre: '{}'", saved.getId(), saved.getFullName());
        return saved;
    }

    public List<RefereeUser> getAllReferees() {
        log.info("Consultando la lista de todos los árbitros");
        List<RefereeUser> referees = refereeRepository.findAll();
        log.info("Total de árbitros obtenidos: {}", referees.size());
        return referees;
    }

    public RefereeUser getRefereeById(Long id) {
        log.info("Buscando árbitro con ID: {}", id);
        RefereeUser referee = refereeRepository.findById(id).orElseThrow(() -> {
            log.warn("Árbitro no encontrado - ID: {}", id);
            return new ResourceNotFoundException("Árbitro con ID " + id + " no encontrado");
        });
        log.info("Árbitro encontrado - ID: {}, nombre: '{}'", id, referee.getFullName());
        return referee;
    }

    public List<Match> getRefereeMatches(Long id) {
        log.info("Consultando partidos asignados al árbitro ID: {}", id);
        RefereeUser referee = getRefereeById(id);
        List<Match> matches = referee.getAssignedMatchIds().stream()
                .map(matchId -> matchRepository.findById(matchId).orElse(null))
                .filter(m -> m != null)
                .collect(Collectors.toList());
        log.info("Total de partidos asignados al árbitro ID {}: {}", id, matches.size());
        return matches;
    }
}
