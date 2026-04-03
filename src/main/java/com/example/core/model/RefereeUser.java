package com.example.core.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Árbitro del torneo. Extiende {@link User} para poder autenticarse en el sistema.
 * No implementa {@link Player} porque no participa como jugador.
 * <p>
 * Sus campos propios ({@code licenseNumber}, {@code certificationLevel}) se heredan
 * de los campos protegidos definidos en {@link User} para la estrategia SINGLE_TABLE.
 * Los partidos asignados se persisten en la tabla {@code referee_matches}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@DiscriminatorValue("REFEREE")
public class RefereeUser extends User {

    @ElementCollection
    @CollectionTable(name = "referee_matches", joinColumns = @JoinColumn(name = "referee_id"))
    @Column(name = "match_id")
    private List<Long> assignedMatchIds = new ArrayList<>();

    @Override
    public String getUserType() { return "REFEREE"; }

    public void registerResult(Match match) {}

    public void registerEvent(Object event) {}

    public void issueCard(Player player, String type) {}
}
