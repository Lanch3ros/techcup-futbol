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
        log.info("Registrando árbitro: {}", request.getFullName());
        Referee referee = new Referee();
        referee.setFullName(request.getFullName());
        referee.setEmail(request.getEmail());
        referee.setLicenseNumber(request.getLicenseNumber());
        referee.setCertificationLevel(request.getCertificationLevel());
        return refereeRepository.save(referee);
    }

    public List<Referee> getAllReferees() {
        return refereeRepository.findAll();
    }

    public Referee getRefereeById(Long id) {
        Referee referee = refereeRepository.findById(id);
        if (referee == null) throw new ResourceNotFoundException("Árbitro con ID " + id + " no encontrado");
        return referee;
    }

    public List<Match> getRefereeMatches(Long id) {
        Referee referee = getRefereeById(id);
        return referee.getAssignedMatchIds().stream()
                .map(matchId -> matchRepository.findById(matchId))
                .filter(m -> m != null)
                .collect(Collectors.toList());
    }
}