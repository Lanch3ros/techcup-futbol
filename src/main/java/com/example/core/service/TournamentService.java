package com.example.core.service;

import com.example.core.model.Tournament;
import com.example.repository.TournamentRepository;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;

    public TournamentService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    public Tournament createTournament(Tournament tournament) {
        log.info("Iniciando creación de nuevo torneo.");

        if (tournament.getEndDate().isBefore(tournament.getStartDate())) {
            log.warn("Intento de crear torneo con fecha de fin anterior a la de inicio.");
            throw new BusinessRuleException("La fecha de finalización no puede ser anterior a la fecha de inicio.");
        }

        Tournament savedTournament = tournamentRepository.save(tournament);
        log.info("Torneo creado exitosamente con ID: {} y estado: {}", savedTournament.getId(), savedTournament.getStatus());

        return savedTournament;
    }

    public List<Tournament> getAllTournaments() {
        log.info("Consultando la lista de todos los torneos.");
        return tournamentRepository.findAll();
    }

    public Tournament getTournamentById(Long id) {
        log.info("Buscando torneo con ID: {}", id);

        Tournament tournament = tournamentRepository.findById(id);
        if (tournament == null) {
            log.error("Fallo en la búsqueda: Torneo {} no encontrado", id);
            throw new ResourceNotFoundException("El torneo con ID " + id + " no existe en el sistema.");
        }

        return tournament;
    }

    public void updateTournamentStatus(Long id, String newStatus) {
        log.info("Actualizando estado del torneo {} a {}", id, newStatus);

        Tournament tournament = getTournamentById(id);

        List<String> validStatuses = List.of("Borrador", "Activo", "En progreso", "Finalizado");

        if (!validStatuses.contains(newStatus)) {
            log.warn("Intento de cambiar torneo a un estado no válido: {}", newStatus);
            throw new BusinessRuleException("Estado inválido. Los estados permitidos son: Borrador, Activo, En progreso, Finalizado.");
        }

        tournament.setStatus(newStatus);
        tournamentRepository.save(tournament);

        log.info("Estado del torneo actualizado correctamente.");
    }
}