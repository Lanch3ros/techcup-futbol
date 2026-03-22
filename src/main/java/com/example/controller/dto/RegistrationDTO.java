package com.example.controller.dto;

public record RegistrationDTO(
        String fullName,
        String email,
        String password,
        String role,
        String department,
        String program,
        Integer jerseyNumber,
        String position,
        String relationship
) {}