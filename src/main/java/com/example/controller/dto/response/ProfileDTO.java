package com.example.controller.dto.response;

import com.example.core.model.Program;
import java.time.LocalDate;

public record ProfileDTO(
        Long id,
        String fullName,
        String email,
        String userType,
        String profilePhoto,
        Integer jerseyNumber,
        String position,
        String identification,
        String gender,
        LocalDate birthDate,
        Program program,
        Long teamId,
        Integer semester
) {}
