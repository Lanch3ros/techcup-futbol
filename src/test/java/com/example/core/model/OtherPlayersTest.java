package com.example.core.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OtherPlayersTest {

    @Test
    void testAdminPlayerMethods() {
        AdminPlayer player = new AdminPlayer();
        assertFalse(player.validateEmail());
        assertEquals("ADMIN", player.getUserType());
        assertNull(player.getProfile());
        assertFalse(player.login());

        player.logout();
        player.acceptInvitation(1L);
        player.rejectInvitation(1L);
        player.setAvailable(true);
    }

    @Test
    void testTeacherPlayerMethods() {
        TeacherPlayer player = new TeacherPlayer();
        assertFalse(player.validateEmail());
        assertEquals("TEACHER", player.getUserType());
        assertNull(player.getProfile());
        assertFalse(player.login());

        player.logout();
        player.acceptInvitation(1L);
        player.rejectInvitation(1L);
        player.setAvailable(true);
    }

    @Test
    void testGraduatePlayerMethods() {
        GraduatePlayer player = new GraduatePlayer();
        assertFalse(player.validateEmail());
        assertEquals("GRADUATE", player.getUserType());
        assertNull(player.getProfile());
        assertFalse(player.login());

        player.logout();
        player.acceptInvitation(1L);
        player.rejectInvitation(1L);
        player.setAvailable(true);
    }

    @Test
    void testRelativePlayerMethods() {
        RelativePlayer player = new RelativePlayer();
        assertFalse(player.validateEmail());
        assertEquals("RELATIVE", player.getUserType());
        assertNull(player.getProfile());
        assertFalse(player.login());

        player.logout();
        player.acceptInvitation(1L);
        player.rejectInvitation(1L);
        player.setAvailable(true);
    }
}