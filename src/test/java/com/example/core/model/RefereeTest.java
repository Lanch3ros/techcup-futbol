package com.example.core.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class RefereeTest {

    @Test
    void testLombokMethods() {
        Referee referee = new Referee("LIC-123", "FIFA", new ArrayList<>());

        assertEquals("LIC-123", referee.getLicenseNumber());
        assertEquals("FIFA", referee.getCertificationLevel());

        Referee referee2 = new Referee();
        referee2.setLicenseNumber("LIC-123");
        referee2.setCertificationLevel("FIFA");
        referee2.setAssignedMatches(new ArrayList<>());

        assertEquals(referee, referee2);
        assertEquals(referee.hashCode(), referee2.hashCode());
        assertNotNull(referee.toString());
    }

    @Test
    void testCustomMethods() {
        Referee referee = new Referee();

        assertNull(referee.getAssignedMatches());

        referee.registerResult(new Match());
        referee.registerEvent(new Object());
        referee.issueCard(new StudentPlayer(), "Red");
    }
}