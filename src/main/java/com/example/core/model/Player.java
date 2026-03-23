package com.example.core.model;

public interface Player {
    Long getId();
    void setId(Long id);

    String getFullName();
    String getEmail();
    String getProfilePhoto();
    boolean validateEmail();
    void acceptInvitation(Long teamId);
    void rejectInvitation(Long teamId);
    void setAvailable(boolean available);
    String getUserType();

    void setFullName(String jugadorNuevo);
}