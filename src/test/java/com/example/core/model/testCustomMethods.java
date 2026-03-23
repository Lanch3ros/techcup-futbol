package com.example.core.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TeamTest {

    @Test
    void testCustomMethods() {
        Team team = new Team();

        team.addPlayer(new StudentPlayer());
        team.removePlayer(1L);

        assertFalse(team.validateCapacity());
        assertNull(team.getAvailablePlayers());
    }
}