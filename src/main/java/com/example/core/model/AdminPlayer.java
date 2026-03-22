package com.example.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AdminPlayer extends User implements Player {
    private String department;
    private String position;
    private int jerseyNumber;
    private boolean available;
    private Long teamId;

    @Override
    public boolean validateEmail() { return false; }

    @Override
    public void acceptInvitation(Long teamId) {}

    @Override
    public void rejectInvitation(Long teamId) {}

    @Override
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String getUserType() { return "ADMIN"; }

    @Override
    public boolean login() {
        return false;
    }

    @Override
    public void logout() {

    }
}
