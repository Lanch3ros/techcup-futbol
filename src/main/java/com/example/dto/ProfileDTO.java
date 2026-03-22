package com.example.dto;

public record ProfileDTO(
        String fullName,
        String email,
        String role,
        String profilePhoto,
        Integer jerseyNumber,
        String position
) {}