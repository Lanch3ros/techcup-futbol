package com.example.core.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Player – default interface method getProgram()")
class PlayerInterfaceTest {

    /**
     * Anonymous implementation that does NOT extend User, so the default
     * Player.getProgram() is not overridden and JaCoCo can record its coverage.
     */
    private static final Player ANONYMOUS_PLAYER = new Player() {
        @Override public Long getId()                          { return null; }
        @Override public void setId(Long id)                   {}
        @Override public void setFullName(String name)         {}
        @Override public String getFullName()                  { return null; }
        @Override public String getEmail()                     { return null; }
        @Override public String getProfilePhoto()              { return null; }
        @Override public boolean validateEmail()               { return false; }
        @Override public void acceptInvitation(Long teamId)    {}
        @Override public void rejectInvitation(Long teamId)    {}
        @Override public void setAvailable(boolean available)  {}
        @Override public String getUserType()                  { return null; }
        @Override public void setPosition(String position)     {}
        @Override public String getPosition()                  { return null; }
        @Override public boolean isAvailable()                 { return false; }
        @Override public void setJerseyNumber(Integer num)     {}
        @Override public Integer getJerseyNumber()             { return null; }
        @Override public void setTeamId(Long teamId)           {}
        @Override public Long getTeamId()                      { return null; }
    };

    @Test
    @DisplayName("getProgram() default retorna null cuando no está sobreescrito")
    void getProgram_DefaultImplementation_ReturnsNull() {
        assertNull(ANONYMOUS_PLAYER.getProgram(),
                "La implementación por defecto de getProgram() debe retornar null");
    }
}
