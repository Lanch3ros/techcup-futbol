package com.example.core.service;

import com.example.core.model.Match;
import com.example.core.model.Team;
import com.example.core.model.Tournament;
import com.example.repository.MatchRepository;
import com.example.repository.TeamRepository;
import com.example.repository.TournamentRepository;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    public TournamentService(TournamentRepository tournamentRepository,
                             TeamRepository teamRepository,
                             MatchRepository matchRepository) {
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
    }

    public Tournament createTournament(Tournament tournament) {
        log.info("Iniciando creación de nuevo torneo.");

        if (tournament.getEndDate().isBefore(tournament.getStartDate())) {
            throw new BusinessRuleException("La fecha de finalización no puede ser anterior a la fecha de inicio.");
        }

        Tournament savedTournament = tournamentRepository.save(tournament);
        log.info("Torneo creado exitosamente con ID: {}", savedTournament.getId());
        return savedTournament;
    }

    public List<Tournament> getAllTournaments() {
        log.info("Consultando la lista de todos los torneos.");
        return tournamentRepository.findAll();
    }

    public Tournament getTournamentById(Long id) {
        log.info("Buscando torneo con ID: {}", id);
        Tournament tournament = tournamentRepository.findById(id);
        if (tournament == null) throw new ResourceNotFoundException("El torneo con ID " + id + " no existe.");
        return tournament;
    }

    public void updateTournamentStatus(Long id, String newStatus) {
        log.info("Actualizando estado del torneo {} a {}", id, newStatus);
        Tournament tournament = getTournamentById(id);

        List<String> validStatuses = List.of("Borrador", "Activo", "En progreso", "Finalizado");
        if (!validStatuses.contains(newStatus)) {
            throw new BusinessRuleException("Estado inválido. Los estados permitidos son: Borrador, Activo, En progreso, Finalizado.");
        }

        tournament.setStatus(newStatus);
        tournamentRepository.save(tournament);
        log.info("Estado del torneo actualizado correctamente.");
    }

    public List<Team> getTournamentTeams(Long tournamentId) {
        log.info("Consultando equipos del torneo {}", tournamentId);
        Tournament tournament = getTournamentById(tournamentId);
        return tournament.getRegisteredTeams() != null ? tournament.getRegisteredTeams() : new ArrayList<>();
    }

    public void registerTeamToTournament(Long tournamentId, Long teamId) {
        log.info("Inscribiendo equipo {} al torneo {}", teamId, tournamentId);

        Tournament tournament = getTournamentById(tournamentId);
        Team team = teamRepository.findById(teamId);

        if (team == null) throw new ResourceNotFoundException("Equipo con ID " + teamId + " no encontrado");

        if (!"Activo".equals(tournament.getStatus())) {
            throw new BusinessRuleException("Solo se pueden inscribir equipos en torneos con estado 'Activo'.");
        }

        if (tournament.getRegisteredTeams() == null) tournament.setRegisteredTeams(new ArrayList<>());

        boolean alreadyRegistered = tournament.getRegisteredTeams().stream().anyMatch(t -> t.getId().equals(teamId));
        if (alreadyRegistered) throw new BusinessRuleException("El equipo ya está inscrito en este torneo.");

        if (tournament.getRegisteredTeams().size() >= tournament.getMaxTeams()) {
            throw new BusinessRuleException("El torneo ya alcanzó el número máximo de equipos.");
        }

        tournament.getRegisteredTeams().add(team);
        team.setTournamentId(tournamentId);

        tournamentRepository.save(tournament);
        teamRepository.save(team);
        log.info("Equipo {} inscrito exitosamente al torneo {}.", teamId, tournamentId);
    }

    public List<Match> generateMatches(Long tournamentId) {
        log.info("Generando partidos para el torneo {}", tournamentId);

        Tournament tournament = getTournamentById(tournamentId);
        List<Team> teams = tournament.getRegisteredTeams();

        if (teams == null || teams.size() < 2) {
            throw new BusinessRuleException("Se necesitan al menos 2 equipos para generar partidos.");
        }

        List<Team> shuffled = new ArrayList<>(teams);
        Collections.shuffle(shuffled);

        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < shuffled.size() - 1; i += 2) {
            Match match = new Match();
            match.setHomeTeam(shuffled.get(i));
            match.setAwayTeam(shuffled.get(i + 1));
            match.setStatus("Programado");
            match.setMatchDate(LocalDateTime.now().plusDays(7));
            match.setEvents(new ArrayList<>());
            match.setLineups(new ArrayList<>());
            matches.add(matchRepository.save(match));
        }

        if (tournament.getMatches() == null) tournament.setMatches(new ArrayList<>());
        tournament.getMatches().addAll(matches);
        tournamentRepository.save(tournament);

        log.info("{} partidos generados para el torneo {}.", matches.size(), tournamentId);
        return matches;
    }
}