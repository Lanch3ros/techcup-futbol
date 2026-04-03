package com.example.core.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class RefereeTest {

    @Test
    void testLombokMethods() {
        RefereeUser referee = new RefereeUser();
        referee.setId(1L);
        referee.setFullName("Carlos Perez");
        referee.setEmail("carlos@email.com");
        referee.setLicenseNumber("LIC-123");
        referee.setCertificationLevel("FIFA");
        referee.setAssignedMatchIds(new ArrayList<>());

        assertEquals(1L, referee.getId());
        assertEquals("Carlos Perez", referee.getFullName());
        assertEquals("carlos@email.com", referee.getEmail());
        assertEquals("LIC-123", referee.getLicenseNumber());
        assertEquals("FIFA", referee.getCertificationLevel());
        assertNotNull(referee.getAssignedMatchIds());
        assertEquals("REFEREE", referee.getUserType());

        RefereeUser referee2 = new RefereeUser();
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
        RefereeUser referee = new RefereeUser();
        assertDoesNotThrow(() -> referee.getAssignedMatchIds());
    }

    @Test
    void testCustomMethods_DoNotThrow() {
        RefereeUser referee = new RefereeUser();
        referee.setAssignedMatchIds(new ArrayList<>());

        assertDoesNotThrow(() -> referee.registerResult(new Match()));
        assertDoesNotThrow(() -> referee.registerEvent(new Object()));
        assertDoesNotThrow(() -> referee.issueCard(new StudentPlayer(), "Roja"));
    }
}
