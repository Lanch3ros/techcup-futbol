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
        log.info("Iniciando creación de torneo - inicio: {}, fin: {}", tournament.getStartDate(), tournament.getEndDate());

        if (tournament.getEndDate().isBefore(tournament.getStartDate())) {
            log.warn("Fecha de fin anterior a la de inicio - inicio: {}, fin: {}", tournament.getStartDate(), tournament.getEndDate());
            throw new BusinessRuleException("La fecha de finalización no puede ser anterior a la fecha de inicio.");
        }

        Tournament savedTournament = tournamentRepository.save(tournament);
        log.info("Torneo creado exitosamente - ID: {}, estado: {}", savedTournament.getId(), savedTournament.getStatus());
        return savedTournament;
    }

    public List<Tournament> getAllTournaments() {
        log.info("Consultando la lista de todos los torneos");
        List<Tournament> tournaments = tournamentRepository.findAll();
        log.info("Total de torneos obtenidos: {}", tournaments.size());
        return tournaments;
    }

    public Tournament getTournamentById(Long id) {
        log.info("Buscando torneo con ID: {}", id);
        Tournament tournament = tournamentRepository.findById(id).orElseThrow(() -> {
            log.warn("Torneo no encontrado - ID: {}", id);
            return new ResourceNotFoundException("El torneo con ID " + id + " no existe.");
        });
        log.info("Torneo encontrado - ID: {}, estado: {}", id, tournament.getStatus());
        return tournament;
    }

    public void updateTournamentStatus(Long id, String newStatus) {
        log.info("Actualizando estado del torneo ID: {} a '{}'", id, newStatus);
        Tournament tournament = getTournamentById(id);

        List<String> validStatuses = List.of("Borrador", "Activo", "En progreso", "Finalizado");
        if (!validStatuses.contains(newStatus)) {
            log.warn("Estado inválido '{}' para torneo ID: {}", newStatus, id);
            throw new BusinessRuleException("Estado inválido. Los estados permitidos son: Borrador, Activo, En progreso, Finalizado.");
        }

        tournament.setStatus(newStatus);
        tournamentRepository.save(tournament);
        log.info("Estado del torneo ID: {} actualizado exitosamente a '{}'", id, newStatus);
    }

    public List<Team> getTournamentTeams(Long tournamentId) {
        log.info("Consultando equipos inscritos en torneo ID: {}", tournamentId);
        Tournament tournament = getTournamentById(tournamentId);
        List<Team> teams = tournament.getRegisteredTeams() != null ? tournament.getRegisteredTeams() : new ArrayList<>();
        log.info("Total de equipos en torneo ID {}: {}", tournamentId, teams.size());
        return teams;
    }

    public void registerTeamToTournament(Long tournamentId, Long teamId) {
        log.info("Inscribiendo equipo ID: {} al torneo ID: {}", teamId, tournamentId);

        Tournament tournament = getTournamentById(tournamentId);

        Team team = teamRepository.findById(teamId).orElseThrow(() -> {
            log.warn("Equipo no encontrado al inscribir al torneo - ID: {}", teamId);
            return new ResourceNotFoundException("Equipo con ID " + teamId + " no encontrado");
        });

        if (!"Activo".equalsIgnoreCase(tournament.getStatus())) {
            log.warn("Intento de inscripción en torneo ID: {} con estado '{}' no permitido", tournamentId, tournament.getStatus());
            throw new BusinessRuleException("Solo se pueden inscribir equipos en torneos con estado 'Activo'.");
        }

        if (tournament.getRegisteredTeams() == null) tournament.setRegisteredTeams(new ArrayList<>());

        boolean alreadyRegistered = tournament.getRegisteredTeams().stream().anyMatch(t -> t.getId().equals(teamId));
        if (alreadyRegistered) {
            log.warn("Equipo ID: {} ya está inscrito en torneo ID: {}", teamId, tournamentId);
            throw new BusinessRuleException("El equipo ya está inscrito en este torneo.");
        }

        if (tournament.getRegisteredTeams().size() >= tournament.getMaxTeams()) {
            log.warn("Torneo ID: {} alcanzó el número máximo de equipos: {}", tournamentId, tournament.getMaxTeams());
            throw new BusinessRuleException("El torneo ya alcanzó el número máximo de equipos.");
        }

        tournament.getRegisteredTeams().add(team);
        team.setTournamentId(tournamentId);

        tournamentRepository.save(tournament);
        teamRepository.save(team);
        log.info("Equipo ID: {} inscrito exitosamente al torneo ID: {}", teamId, tournamentId);
    }

    public List<Match> generateMatches(Long tournamentId) {
        log.info("Generando partidos para torneo ID: {}", tournamentId);

        Tournament tournament = getTournamentById(tournamentId);
        List<Team> teams = tournament.getRegisteredTeams();

        if (teams == null || teams.size() < 2) {
            log.warn("Torneo ID: {} no tiene suficientes equipos para generar partidos - total: {}", tournamentId, teams != null ? teams.size() : 0);
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

        log.info("{} partidos generados exitosamente para torneo ID: {}", matches.size(), tournamentId);
        return matches;
    }

    public List<Match> generateQuarterFinals(Long tournamentId) {
        log.info("Generando cuartos de final para torneo ID: {}", tournamentId);

        Tournament tournament = getTournamentById(tournamentId);

        if (!"En progreso".equalsIgnoreCase(tournament.getStatus())) {
            log.warn("Torneo ID: {} no está en estado 'En progreso' - estado actual: '{}'", tournamentId, tournament.getStatus());
            throw new BusinessRuleException("Los cuartos de final solo pueden generarse cuando el torneo está en estado 'En progreso'.");
        }

        boolean alreadyGenerated = tournament.getMatches() != null && tournament.getMatches().stream()
                .anyMatch(m -> "Cuartos de Final".equalsIgnoreCase(m.getPhase()));
        if (alreadyGenerated) {
            log.warn("Los cuartos de final ya fueron generados para el torneo ID: {}", tournamentId);
            throw new BusinessRuleException("Los cuartos de final ya han sido generados para este torneo.");
        }

        List<Team> registered = tournament.getRegisteredTeams();
        if (registered == null || registered.size() < 8) {
            log.warn("Torneo ID: {} no tiene suficientes equipos para cuartos de final - total: {}",
                    tournamentId, registered != null ? registered.size() : 0);
            throw new BusinessRuleException("Se necesitan al menos 8 equipos clasificados para generar los cuartos de final.");
        }

        // Clasificar por puntos DESC, diferencia de goles DESC, goles a favor DESC
        List<Team> seeded = registered.stream()
                .sorted(Comparator.comparingInt(Team::getPoints).reversed()
                        .thenComparingInt(Team::getGoalDifference).reversed()
                        .thenComparingInt(Team::getGoalsFor).reversed())
                .limit(8)
                .toList();

        log.info("Top 8 clasificados para torneo ID: {}: {}",
                tournamentId, seeded.stream().map(Team::getName).toList());

        // Seeding clásico: 1v8, 2v7, 3v6, 4v5
        int[][] pairings = {{0, 7}, {1, 6}, {2, 5}, {3, 4}};
        List<Match> quarterFinals = new ArrayList<>();
        LocalDateTime kickoff = LocalDateTime.now().plusDays(7);

        for (int[] pair : pairings) {
            Team home = seeded.get(pair[0]);
            Team away = seeded.get(pair[1]);

            Match match = new Match();
            match.setHomeTeam(home);
            match.setAwayTeam(away);
            match.setPhase("Cuartos de Final");
            match.setStatus("Programado");
            match.setMatchDate(kickoff);
            match.setEvents(new ArrayList<>());
            match.setLineups(new ArrayList<>());
            quarterFinals.add(matchRepository.save(match));

            log.info("Cuarto de final generado: {} vs {}", home.getName(), away.getName());
        }

        if (tournament.getMatches() == null) tournament.setMatches(new ArrayList<>());
        tournament.getMatches().addAll(quarterFinals);
        tournamentRepository.save(tournament);

        log.info("4 cuartos de final generados exitosamente para torneo ID: {}", tournamentId);
        return quarterFinals;
    }
}