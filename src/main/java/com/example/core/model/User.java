package com.example.core.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public abstract class User implements Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected String identification;
    protected String email;
    protected String password;

    @Column(name = "full_name")
    protected String fullName;

    protected String role;

    @Column(name = "profile_photo")
    protected String profilePhoto;

    // ── Campos comunes a todos los tipos de jugador ──────────────────────────
    @Column(name = "jersey_number")
    protected Integer jerseyNumber;

    protected String position;

    protected boolean available = true;

    @Column(name = "team_id")
    protected Long teamId;

    // ── Campos específicos por subtipo (nullable según discriminador) ─────────
    @Enumerated(EnumType.STRING)
    protected Program program;       // STUDENT, GRADUATE

    protected String department;     // STUDENT, TEACHER, ADMIN

    protected String relationship;   // RELATIVE

    // Métodos de Player que los subtipos deben implementar
    public abstract boolean validateEmail();
    public abstract void acceptInvitation(Long teamId);
    public abstract void rejectInvitation(Long teamId);
    public abstract String getUserType();

    public abstract boolean login();
    public abstract void logout();
}
