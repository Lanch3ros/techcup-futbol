package com.example.core.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Invitation – @PrePersist prePersist()")
class InvitationTest {

    private void invokePrePersist(Invitation invitation) throws Exception {
        Method prePersist = Invitation.class.getDeclaredMethod("prePersist");
        prePersist.setAccessible(true);
        prePersist.invoke(invitation);
    }

    @Test
    @DisplayName("prePersist – status null → se establece PENDING y createdAt no-null")
    void prePersist_StatusNull_SetsPendingAndCreatedAt() throws Exception {
        Invitation inv = new Invitation();
        inv.setPlayerId(1L);
        inv.setTeamId(2L);
        // status is null by default

        invokePrePersist(inv);

        assertEquals(Invitation.PENDING, inv.getStatus(),
                "status null debe inicializarse a PENDING");
        assertNotNull(inv.getCreatedAt(),
                "createdAt debe ser asignado por prePersist");
        assertTrue(inv.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)),
                "createdAt debe ser una fecha/hora cercana a 'ahora'");
    }

    @Test
    @DisplayName("prePersist – status ya establecido → status no cambia, createdAt sí se asigna")
    void prePersist_StatusAlreadySet_DoesNotOverrideStatus() throws Exception {
        Invitation inv = new Invitation();
        inv.setPlayerId(1L);
        inv.setTeamId(2L);
        inv.setStatus(Invitation.ACCEPTED);

        invokePrePersist(inv);

        assertEquals(Invitation.ACCEPTED, inv.getStatus(),
                "status preexistente no debe ser sobreescrito");
        assertNotNull(inv.getCreatedAt(),
                "createdAt debe ser asignado siempre");
    }

    @Test
    @DisplayName("Constantes de estado tienen los valores esperados")
    void statusConstants_HaveExpectedValues() {
        assertEquals("PENDING",  Invitation.PENDING);
        assertEquals("ACCEPTED", Invitation.ACCEPTED);
        assertEquals("REJECTED", Invitation.REJECTED);
    }
}
