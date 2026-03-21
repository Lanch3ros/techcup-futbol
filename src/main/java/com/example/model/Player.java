package com.example.model;

public interface Player {
    boolean validateEmail();
    void acceptInvitation(Long teamId);
    void rejectInvitation(Long teamId);
    void setAvailable(boolean available);
    String getUserType();
}