package com.example.core.service;

import com.example.controller.dto.request.RefereeRequest;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.Match;
import com.example.core.model.RefereeUser;
import com.example.repository.MatchRepository;
import com.example.repository.RefereeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("RefereeService – Gestión de árbitros")
class RefereeServiceTest {

    private RefereeRepository refereeRepository;
    private MatchRepository matchRepository;
    private RefereeService refereeService;

    @BeforeEach
    void setUp() {
        refereeRepository = mock(RefereeRepository.class);
        matchRepository   = mock(MatchRepository.class);
        refereeService    = new RefereeService(refereeRepository, matchRepository);
    }

    // ── createReferee ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createReferee – persiste y devuelve árbitro con todos los campos")
    void createReferee_Success() {
        RefereeRequest req = new RefereeRequest();
        req.setFullName("Carlos Árbitro");
        req.setEmail("carlos@arbitro.com");
        req.setLicenseNumber("LIC-001");
        req.setCertificationLevel("FIFA");

        RefereeUser saved = new RefereeUser();
        saved.setId(1L);
        saved.setFullName("Carlos Árbitro");
        saved.setEmail("carlos@arbitro.com");
        saved.setLicenseNumber("LIC-001");
        saved.setCertificationLevel("FIFA");
        when(refereeRepository.save(any())).thenReturn(saved);

        RefereeUser result = refereeService.createReferee(req);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Carlos Árbitro", result.getFullName());
        verify(refereeRepository).save(any(RefereeUser.class));
    }

    // ── getAllReferees ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllReferees – devuelve lista completa")
    void getAllReferees_ReturnsList() {
        RefereeUser r1 = new RefereeUser(); r1.setId(1L);
        RefereeUser r2 = new RefereeUser(); r2.setId(2L);
        when(refereeRepository.findAll()).thenReturn(List.of(r1, r2));

        List<RefereeUser> result = refereeService.getAllReferees();
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("getAllReferees – lista vacía")
    void getAllReferees_Empty() {
        when(refereeRepository.findAll()).thenReturn(List.of());
        assertTrue(refereeService.getAllReferees().isEmpty());
    }

    // ── getRefereeById ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getRefereeById – árbitro encontrado")
    void getRefereeById_Found() {
        RefereeUser r = new RefereeUser(); r.setId(1L); r.setFullName("Árbitro A");
        when(refereeRepository.findById(1L)).thenReturn(Optional.of(r));

        RefereeUser result = refereeService.getRefereeById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("getRefereeById – no encontrado → ResourceNotFoundException")
    void getRefereeById_NotFound() {
        when(refereeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> refereeService.getRefereeById(99L));
    }

    // ── getRefereeMatches ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getRefereeMatches – devuelve partidos asignados")
    void getRefereeMatches_WithMatches() {
        RefereeUser referee = new RefereeUser();
        referee.setId(1L);
        referee.setAssignedMatchIds(new ArrayList<>(List.of(10L, 11L)));

        Match m1 = new Match(); m1.setId(10L);
        Match m2 = new Match(); m2.setId(11L);

        when(refereeRepository.findById(1L)).thenReturn(Optional.of(referee));
        when(matchRepository.findById(10L)).thenReturn(Optional.of(m1));
        when(matchRepository.findById(11L)).thenReturn(Optional.of(m2));

        List<Match> result = refereeService.getRefereeMatches(1L);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("getRefereeMatches – partido no encontrado es filtrado (rama m != null)")
    void getRefereeMatches_MissingMatchFiltered() {
        RefereeUser referee = new RefereeUser();
        referee.setId(1L);
        referee.setAssignedMatchIds(new ArrayList<>(List.of(10L, 99L)));

        Match m1 = new Match(); m1.setId(10L);

        when(refereeRepository.findById(1L)).thenReturn(Optional.of(referee));
        when(matchRepository.findById(10L)).thenReturn(Optional.of(m1));
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        List<Match> result = refereeService.getRefereeMatches(1L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getRefereeMatches – sin partidos asignados → lista vacía")
    void getRefereeMatches_NoMatches() {
        RefereeUser referee = new RefereeUser();
        referee.setId(1L);
        referee.setAssignedMatchIds(new ArrayList<>());

        when(refereeRepository.findById(1L)).thenReturn(Optional.of(referee));

        List<Match> result = refereeService.getRefereeMatches(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getRefereeMatches – árbitro no encontrado → ResourceNotFoundException")
    void getRefereeMatches_RefereeNotFound() {
        when(refereeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> refereeService.getRefereeMatches(99L));
    }
}
