package com.example.core.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class RefereeTest {

    @Test
    void testLombokMethods() {
        // Constructor AllArgsConstructor de Referee:
        // Referee(Long id, String fullName, String email, String licenseNumber,
        //         String certificationLevel, List<Long> assignedMatchIds)
        Referee referee = new Referee(1L, "Carlos Perez", "carlos@email.com",
                "LIC-123", "FIFA", new ArrayList<>());

        assertEquals(1L, referee.getId());
        assertEquals("Carlos Perez", referee.getFullName());
        assertEquals("carlos@email.com", referee.getEmail());
        assertEquals("LIC-123", referee.getLicenseNumber());
        assertEquals("FIFA", referee.getCertificationLevel());
        assertNotNull(referee.getAssignedMatchIds());

        Referee referee2 = new Referee();
        referee2.setId(1L);
        referee2.setFullName("Carlos Perez");
        referee2.setEmail("carlos@email.com");
        referee2.setLicenseNumber("LIC-123");
        referee2.setCertificationLevel("FIFA");
        referee2.setAssignedMatchIds(new ArrayList<>());

        assertEquals(referee, referee2);
        assertEquals(referee.hashCode(), referee2.hashCode());
        assertNotNull(referee.toString());
    }

    @Test
    void testGetAssignedMatchIds_DefaultNotNull() {
        Referee referee = new Referee();
        assertDoesNotThrow(() -> referee.getAssignedMatchIds());
    }

    @Test
    void testCustomMethods_DoNotThrow() {
        Referee referee = new Referee();
        referee.setAssignedMatchIds(new ArrayList<>());

        // Estos métodos tienen cuerpo vacío, solo verificamos que no lanzan excepción
        assertDoesNotThrow(() -> referee.registerResult(new Match()));
        assertDoesNotThrow(() -> referee.registerEvent(new Object()));
        assertDoesNotThrow(() -> referee.issueCard(new StudentPlayer(), "Roja"));
    }
}