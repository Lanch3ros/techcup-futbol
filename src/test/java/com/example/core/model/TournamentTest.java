package com.example.core.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TournamentTest {

    @Test
    void testCustomMethods() {
        Tournament tournament = new Tournament();

        tournament.registerTeam(new Team());
        tournament.closeRegistration();
        tournament.generateMatches();

        assertNull(tournament.getCalendar());
        assertFalse(tournament.validateDates());
    }
}