package com.example.core.factory;

import com.example.controller.dto.RegistrationDTO;
import com.example.core.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OtherFactoriesTest {

    @Test
    void adminFactory_CreatesAdminPlayer() {
        AdminFactory factory = new AdminFactory();
        RegistrationDTO dto = new RegistrationDTO("Admin", "admin@escuelaing.edu.co", "123", "ADMIN", null, null, null, null, null);
        Player result = factory.registerPlayerData(dto);
        assertTrue(result instanceof AdminPlayer);
    }

    @Test
    void teacherFactory_CreatesTeacherPlayer() {
        TeacherFactory factory = new TeacherFactory();
        RegistrationDTO dto = new RegistrationDTO("Teacher", "teacher@escuelaing.edu.co", "123", "TEACHER", null, null, null, null, null);
        Player result = factory.registerPlayerData(dto);
        assertTrue(result instanceof TeacherPlayer);
    }

    @Test
    void graduateFactory_CreatesGraduatePlayer() {
        GraduateFactory factory = new GraduateFactory();
        RegistrationDTO dto = new RegistrationDTO("Graduate", "graduate@mail.escuelaing.edu.co", "123", "GRADUATE", null, null, null, null, null);
        Player result = factory.registerPlayerData(dto);
        assertTrue(result instanceof GraduatePlayer);
    }

    @Test
    void relativeFactory_CreatesRelativePlayer() {
        RelativeFactory factory = new RelativeFactory();
        RegistrationDTO dto = new RegistrationDTO("Relative", "relative@gmail.com", "123", "RELATIVE", null, null, null, null, null);
        Player result = factory.registerPlayerData(dto);
        assertTrue(result instanceof RelativePlayer);
    }
}