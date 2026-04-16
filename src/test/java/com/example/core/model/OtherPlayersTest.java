package com.example.core.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OtherPlayersTest {

    @Test
    void testAdminUserMethods() {
        AdminUser user = new AdminUser();
        assertEquals("ADMIN", user.getUserType());
        assertNull(user.getProfile());
        assertFalse(user.login());
        assertNull(user.getProfilePhoto());
        user.logout();
    }

    @Test
    void testTeacherPlayerMethods() {
        TeacherPlayer player = new TeacherPlayer();
        assertFalse(player.validateEmail());
        assertEquals("TEACHER", player.getUserType());
        assertNull(player.getProfile());
        assertFalse(player.login());
        assertNull(player.getProfilePhoto());

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
        assertNull(player.getProfilePhoto());

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
        assertNull(player.getProfilePhoto());

        player.logout();
        player.acceptInvitation(1L);
        player.rejectInvitation(1L);
        player.setAvailable(true);
    }

    @Test
    void testStaffPlayerMethods() {
        StaffPlayer player = new StaffPlayer();
        assertFalse(player.validateEmail());
        assertEquals("STAFF", player.getUserType());
        assertNull(player.getProfile());
        assertFalse(player.login());
        assertNull(player.getProfilePhoto());

        player.logout();
        player.acceptInvitation(1L);
        player.rejectInvitation(1L);
        player.setAvailable(true);
    }
}
