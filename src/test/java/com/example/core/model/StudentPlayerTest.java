package com.example.core.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StudentPlayerTest {

    @Test
    void testLombokMethodsAndInheritance() {
        StudentPlayer player = new StudentPlayer(1L, "Sistemas", "Ingeniería", 10, "Delantero", 100L, true);
        player.setId(1L);
        player.setEmail("estudiante@mail.escuelaing.edu.co");
        player.setPassword("1234");
        player.setFullName("Jose Lancheros");
        player.setRole("STUDENT");
        player.setProfilePhoto("foto.png");

        assertEquals(1L, player.getId());
        assertEquals("Sistemas", player.getDepartment());
        assertEquals("Ingeniería", player.getProgram());
        assertEquals(10, player.getJerseyNumber());
        assertEquals("Delantero", player.getPosition());
        assertEquals(100L, player.getTeamId());
        assertTrue(player.isAvailable());

        assertEquals("estudiante@mail.escuelaing.edu.co", player.getEmail());
        assertEquals("1234", player.getPassword());
        assertEquals("Jose Lancheros", player.getFullName());
        assertEquals("STUDENT", player.getRole());
        assertEquals("foto.png", player.getProfilePhoto());

        StudentPlayer player2 = new StudentPlayer();
        assertNotEquals(player, player2);
        assertNotNull(player.toString());
        assertNotEquals(0, player.hashCode());
    }

    @Test
    void testCustomMethods() {
        StudentPlayer player = new StudentPlayer();

        assertFalse(player.validateEmail());
        assertEquals("STUDENT", player.getUserType());
        assertNull(player.getProfile());
        assertFalse(player.login());

        player.acceptInvitation(1L);
        player.rejectInvitation(1L);
        player.logout();

        player.setAvailable(false);
        assertFalse(player.isAvailable());
    }
}