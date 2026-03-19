package com.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDTO {
    private String fullName;
    private String email;
    private String password;
    private String role;

    private String department;
    private String program;
    private Integer jerseyNumber;
    private String position;
    private String relationship;
}