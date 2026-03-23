package com.example.core.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatchTest {

    @Test
    void testCustomMethods() {
        Match match = new Match();

        match.registerResult(2, 1);
        match.registerEvent(new Object());

        assertNull(match.getRedCards());
        assertNull(match.getYellowCards());
        assertNull(match.determineWinner());
    }
}