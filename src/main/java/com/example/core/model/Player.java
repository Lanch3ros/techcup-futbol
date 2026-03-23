package com.example.core.model;

public interface Player {
    Long getId();
    void setId(Long id);

    boolean validateEmail();
    void acceptInvitation(Long teamId);
    void rejectInvitation(Long teamId);
    void setAvailable(boolean available);
    String getUserType();
}