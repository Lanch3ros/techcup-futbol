package com.example.core.model;

public interface Player {
    default Program getProgram() { return null; }
    Long getId();
    void setId(Long id);

    void setFullName(String jugadorNuevo);
    String getFullName();
    String getEmail();
    String getProfilePhoto();
    boolean validateEmail();
    void acceptInvitation(Long teamId);
    void rejectInvitation(Long teamId);
    void setAvailable(boolean available);
    String getUserType();
    void setPosition(String position);
    String getPosition();
    boolean isAvailable();
    void setJerseyNumber(Integer jerseyNumber);
    Integer getJerseyNumber();
    void setTeamId(Long teamId);
    Long getTeamId();
}