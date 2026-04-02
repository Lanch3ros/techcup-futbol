package com.example.core.service;

import com.example.core.exception.BusinessRuleException;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.Invitation;
import com.example.core.model.StudentPlayer;
import com.example.core.model.User;
import com.example.repository.InvitationRepository;
import com.example.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("PlayerService – Gestión de Invitaciones (RF-11 / RN-11-3)")
class InvitationServiceTest {

    private PlayerRepository playerRepository;
    private InvitationRepository invitationRepository;
    private PlayerService playerService;

    private User player;
    private Invitation pendingInvitation;

    @BeforeEach
    void setUp() {
        playerRepository     = mock(PlayerRepository.class);
        invitationRepository = mock(InvitationRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(anyString())).thenReturn("$2a$10$hashed");

        playerService = new PlayerService(playerRepository, encoder, invitationRepository);

        player = new StudentPlayer();
        player.setId(10L);
        player.setAvailable(true);
        player.setTeamId(null);

        pendingInvitation = new Invitation();
        pendingInvitation.setId(1L);
        pendingInvitation.setPlayerId(10L);
        pendingInvitation.setTeamId(5L);
        pendingInvitation.setStatus(Invitation.PENDING);
    }

    // ── Aceptar invitación ───────────────────────────────────────────────────

    @Test
    @DisplayName("Aceptar → jugador queda vinculado al equipo y no disponible")
    void accept_LinksPlayerToTeamAndSetsUnavailable() {
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(pendingInvitation));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(invitationRepository.findByPlayerIdAndStatusIgnoreCase(10L, Invitation.PENDING)).thenReturn(List.of());

        playerService.processInvitationResponse(1L, "ACCEPT");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(playerRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(5L, saved.getTeamId(), "El teamId debe ser el del equipo que invitó");
        assertFalse(saved.isAvailable(), "El jugador debe quedar no disponible");
    }

    @Test
    @DisplayName("Aceptar → la invitación queda con estado ACCEPTED")
    void accept_InvitationStatusBecomesAccepted() {
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(pendingInvitation));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(invitationRepository.findByPlayerIdAndStatusIgnoreCase(10L, Invitation.PENDING)).thenReturn(List.of());

        playerService.processInvitationResponse(1L, "ACCEPT");

        assertEquals(Invitation.ACCEPTED, pendingInvitation.getStatus());
        verify(invitationRepository).save(pendingInvitation);
    }

    @Test
    @DisplayName("RN-11-3: Aceptar → las demás invitaciones PENDING del jugador quedan REJECTED")
    void accept_OtherPendingInvitationsRejected_RN113() {
        Invitation other1 = new Invitation(); other1.setId(2L); other1.setPlayerId(10L); other1.setTeamId(7L); other1.setStatus(Invitation.PENDING);
        Invitation other2 = new Invitation(); other2.setId(3L); other2.setPlayerId(10L); other2.setTeamId(9L); other2.setStatus(Invitation.PENDING);

        when(invitationRepository.findById(1L)).thenReturn(Optional.of(pendingInvitation));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(invitationRepository.findByPlayerIdAndStatusIgnoreCase(10L, Invitation.PENDING))
                .thenReturn(List.of(other1, other2));

        playerService.processInvitationResponse(1L, "ACCEPT");

        assertEquals(Invitation.REJECTED, other1.getStatus(), "Invitación del equipo 7 debe quedar REJECTED");
        assertEquals(Invitation.REJECTED, other2.getStatus(), "Invitación del equipo 9 debe quedar REJECTED");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Invitation>> captor = ArgumentCaptor.forClass(List.class);
        verify(invitationRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    @DisplayName("RN-11-3: Sin otras invitaciones pendientes → saveAll con lista vacía")
    void accept_NoOtherPending_SaveAllEmpty() {
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(pendingInvitation));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(invitationRepository.findByPlayerIdAndStatusIgnoreCase(10L, Invitation.PENDING)).thenReturn(List.of());

        playerService.processInvitationResponse(1L, "ACCEPT");

        verify(invitationRepository).saveAll(argThat((Iterable<Invitation> it) -> !it.iterator().hasNext()));
    }

    // ── Rechazar invitación ──────────────────────────────────────────────────

    @Test
    @DisplayName("Rechazar → la invitación queda con estado REJECTED")
    void reject_InvitationStatusBecomesRejected() {
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(pendingInvitation));

        playerService.processInvitationResponse(1L, "REJECT");

        assertEquals(Invitation.REJECTED, pendingInvitation.getStatus());
        verify(invitationRepository).save(pendingInvitation);
        verifyNoInteractions(playerRepository);
    }

    @Test
    @DisplayName("Rechazar → el jugador NO es vinculado a ningún equipo")
    void reject_PlayerNotLinkedToTeam() {
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(pendingInvitation));

        playerService.processInvitationResponse(1L, "REJECT");

        verify(playerRepository, never()).save(any());
        assertNull(player.getTeamId());
        assertTrue(player.isAvailable());
    }

    // ── Guards ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Invitación no encontrada → ResourceNotFoundException")
    void invitationNotFound_ThrowsException() {
        when(invitationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> playerService.processInvitationResponse(99L, "ACCEPT"));
    }

    @Test
    @DisplayName("Invitación ya aceptada → BusinessRuleException (idempotencia)")
    void alreadyAccepted_ThrowsException() {
        pendingInvitation.setStatus(Invitation.ACCEPTED);
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(pendingInvitation));

        assertThrows(BusinessRuleException.class,
                () -> playerService.processInvitationResponse(1L, "ACCEPT"));
    }

    @Test
    @DisplayName("Invitación ya rechazada → BusinessRuleException")
    void alreadyRejected_ThrowsException() {
        pendingInvitation.setStatus(Invitation.REJECTED);
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(pendingInvitation));

        assertThrows(BusinessRuleException.class,
                () -> playerService.processInvitationResponse(1L, "REJECT"));
    }

    @Test
    @DisplayName("Aceptar → jugador del invitation no encontrado → ResourceNotFoundException")
    void accept_PlayerNotFound_ThrowsResourceNotFound() {
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(pendingInvitation));
        when(playerRepository.findById(10L)).thenReturn(Optional.empty()); // jugador no existe

        assertThrows(com.example.core.exception.ResourceNotFoundException.class,
                () -> playerService.processInvitationResponse(1L, "ACCEPT"));
    }

    @Test
    @DisplayName("Acción desconocida (ni ACCEPT ni REJECT) → invitación no se modifica (else branch)")
    void unknownAction_NoOp() {
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(pendingInvitation));

        assertDoesNotThrow(() -> playerService.processInvitationResponse(1L, "IGNORAR"));
        // La invitación sigue PENDING, no se guardó ni se rechazó
        assertEquals(Invitation.PENDING, pendingInvitation.getStatus());
        verifyNoInteractions(playerRepository);
    }
}
