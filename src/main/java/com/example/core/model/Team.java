package com.example.core.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String colors;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "shield_url")
    private String shieldUrl;

    // Relación gestionada a nivel de aplicación; se carga vía UserRepository.findByTeamId()
    @Transient
    private List<Player> players;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captain_id")
    private User captain;

    @Column(name = "tournament_id")
    private Long tournamentId;

    // Estadísticas
    @Column(name = "matches_played")
    private int matchesPlayed;

    @Column(name = "matches_won")
    private int matchesWon;

    @Column(name = "matches_drawn")
    private int matchesDrawn;

    @Column(name = "matches_lost")
    private int matchesLost;

    @Column(name = "goals_for")
    private int goalsFor;

    @Column(name = "goals_against")
    private int goalsAgainst;

    @Column(name = "goal_difference")
    private int goalDifference;

    private int points;

    // ── Alineación persistida (Fase 2: GAP-10, GAP-11) ───────────────────────
    @ElementCollection
    @CollectionTable(name = "team_starting_players", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "player_id")
    private List<Long> startingPlayerIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "team_reserve_players", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "player_id")
    private List<Long> reservePlayerIds = new ArrayList<>();

    @Column(name = "formation")
    private String formation;

    public void addPlayer(Player player) {}
    public void removePlayer(Long playerId) {}
    public boolean validateCapacity() { return false; }
    public List<Player> getAvailablePlayers() { return null; }
}
