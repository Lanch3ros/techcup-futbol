package com.example.core.factory;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.model.GraduatePlayer;
import com.example.core.model.RelativePlayer;
import com.example.core.model.StaffPlayer;
import com.example.core.model.TeacherPlayer;
import com.example.core.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OtherFactoriesTest {

    private PlayerRegistrationRequest buildRequest(String name, String email, String role) {
        PlayerRegistrationRequest request = new PlayerRegistrationRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword("12345678");
        request.setUserType(role);
        request.setJerseyNumber(10);
        request.setPosition("Delantero");
        return request;
    }

    @Test
    void teacherFactory_CreatesTeacherPlayer() {
        TeacherFactory factory = new TeacherFactory();
        PlayerRegistrationRequest dto = buildRequest("Teacher", "teacher@escuelaing.edu.co", "TEACHER");
        User result = factory.registerPlayerData(dto);
        assertTrue(result instanceof TeacherPlayer);
    }

    @Test
    void graduateFactory_CreatesGraduatePlayer() {
        GraduateFactory factory = new GraduateFactory();
        PlayerRegistrationRequest dto = buildRequest("Graduate", "graduate@mail.escuelaing.edu.co", "GRADUATE");
        User result = factory.registerPlayerData(dto);
        assertTrue(result instanceof GraduatePlayer);
    }

    @Test
    void relativeFactory_CreatesRelativePlayer() {
        RelativeFactory factory = new RelativeFactory();
        PlayerRegistrationRequest dto = buildRequest("Relative", "relative@gmail.com", "RELATIVE");
        User result = factory.registerPlayerData(dto);
        assertTrue(result instanceof RelativePlayer);
    }

    @Test
    void staffFactory_CreatesStaffPlayer() {
        StaffFactory factory = new StaffFactory();
        PlayerRegistrationRequest dto = buildRequest("Staff", "staff@escuelaing.edu.co", "STAFF");
        User result = factory.registerPlayerData(dto);
        assertTrue(result instanceof StaffPlayer);
    }

    @Test
    void staffFactory_InvalidEmail_ThrowsException() {
        StaffFactory factory = new StaffFactory();
        PlayerRegistrationRequest dto = buildRequest("Staff", "staff@gmail.com", "STAFF");
        assertThrows(IllegalArgumentException.class, () -> factory.registerPlayerData(dto));
    }
}
