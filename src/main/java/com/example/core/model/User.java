package com.example.core.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(unique = true)
    protected String identification;

    @Column(unique = true, nullable = false)
    protected String email;
    protected String password;

    @Column(name = "full_name")
    protected String fullName;

    protected String role;

    @Column(name = "profile_photo")
    protected String profilePhoto;

    // ── Campos comunes a jugadores (nullable para tipos no-jugador) ────────────
    @Column(name = "jersey_number")
    protected Integer jerseyNumber;

    protected String position;

    protected boolean available = true;

    @Column(name = "team_id")
    protected Long teamId;

    // ── Campos específicos por subtipo (nullable según discriminador) ─────────
    @Enumerated(EnumType.STRING)
    protected Program program;       // STUDENT, GRADUATE

    protected String department;     // STUDENT, TEACHER

    protected String relationship;   // RELATIVE

    // ── Campos demográficos (Fase 1: GAP-03) ─────────────────────────────────
    @Column(name = "birth_date")
    protected LocalDate birthDate;

    protected String gender;

    // ── Campos específicos de árbitro (nullable para tipos no-árbitro) ────────
    @Column(name = "license_number", unique = true)
    protected String licenseNumber;

    @Column(name = "certification_level")
    protected String certificationLevel;

    /** Retorna el discriminador del subtipo. Requerido para resolución de roles. */
    public abstract String getUserType();

    /** Login stub — subtipos de jugador/árbitro pueden sobreescribir. */
    public boolean login() { return false; }

    /** Logout stub. */
    public void logout() {}
}
