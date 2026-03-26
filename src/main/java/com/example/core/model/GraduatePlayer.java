package com.example.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GraduatePlayer extends User implements Player {
    private Long id;
    private String program;
    private Integer jerseyNumber;
    private String position;
    private Long teamId;
    private boolean available = true;
    @Override
    public boolean validateEmail() { return false; }

    @Override
    public void acceptInvitation(Long teamId) {}

    @Override
    public void rejectInvitation(Long teamId) {}

    @Override
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String getUserType() { return "GRADUATE"; }

    @Override
    public boolean login() {
        return false;
    }

    @Override
    public void logout() {
    }

    public Object getProfile() { return null; }
}
