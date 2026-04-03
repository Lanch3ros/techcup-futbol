package com.example.core.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

/**
 * Organizador del torneo. Gestiona torneos y equipos pero NO es un jugador
 * y por tanto no implementa la interfaz {@link Player}.
 */
@Entity
@DiscriminatorValue("ORGANIZER")
@NoArgsConstructor
public class OrganizerUser extends User {

    @Override
    public String getUserType() { return "ORGANIZER"; }

    public Object getProfile() { return null; }
}
