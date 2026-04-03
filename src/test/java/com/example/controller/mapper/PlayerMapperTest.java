package com.example.controller.mapper;

import com.example.controller.dto.response.ProfileDTO;
import com.example.core.model.Program;
import com.example.core.model.StudentPlayer;
import com.example.core.model.TeacherPlayer;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMapperTest {

    private final PlayerMapper playerMapper = new PlayerMapper();

    @Test
    void toDto_Success() {
        StudentPlayer player = new StudentPlayer();
        player.setFullName("Jose Lancheros");
        player.setEmail("jose@mail.escuelaing.edu.co");
        player.setProfilePhoto("foto.png");

        ProfileDTO result = playerMapper.toDto(player);

        assertNotNull(result);
        assertEquals("Jose Lancheros", result.fullName());
        assertEquals("jose@mail.escuelaing.edu.co", result.email());
        assertEquals("foto.png", result.profilePhoto());
    }

    @Test
    void toDto_NullInput_ReturnsNull() {
        ProfileDTO result = playerMapper.toDto(null);
        assertNull(result);
    }

    @Test
    void toDto_WithAllNewFields_MapsCorrectly() {
        StudentPlayer player = new StudentPlayer();
        player.setFullName("Maria Lopez");
        player.setEmail("maria@mail.escuelaing.edu.co");
        player.setIdentification("987654321");
        player.setGender("F");
        player.setBirthDate(LocalDate.of(2001, 3, 20));
        player.setProgram(Program.SISTEMAS);
        player.setTeamId(5L);
        player.setSemester(4);

        ProfileDTO result = playerMapper.toDto(player);

        assertNotNull(result);
        assertEquals("STUDENT", result.userType());
        assertEquals("987654321", result.identification());
        assertEquals("F", result.gender());
        assertEquals(LocalDate.of(2001, 3, 20), result.birthDate());
        assertEquals(Program.SISTEMAS, result.program());
        assertEquals(5L, result.teamId());
        assertEquals(4, result.semester());
    }

    @Test
    void toDto_NonStudentPlayer_SemesterIsNull() {
        TeacherPlayer teacher = new TeacherPlayer();
        teacher.setFullName("Prof. García");
        teacher.setEmail("garcia@escuelaing.edu.co");

        ProfileDTO result = playerMapper.toDto(teacher);

        assertNotNull(result);
        assertEquals("TEACHER", result.userType());
        assertNull(result.semester()); // TeacherPlayer is not instanceof StudentPlayer
    }
}