package com.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {
    private String fullName;
    private String email;
    private String role;
    private String profilePhoto;
    private Integer jerseyNumber;
    private String position;
}