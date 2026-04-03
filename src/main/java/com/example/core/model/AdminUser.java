package com.example.core.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

/**
 * Administrador del sistema. Tiene acceso total pero NO es un jugador
 * y por tanto no implementa la interfaz {@link Player}.
 */
@Entity
@DiscriminatorValue("ADMIN")
@NoArgsConstructor
public class AdminUser extends User {

    @Override
    public String getUserType() { return "ADMIN"; }

    public Object getProfile() { return null; }
}
