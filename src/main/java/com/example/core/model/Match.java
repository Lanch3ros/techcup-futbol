package com.example.core.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_date")
    private LocalDateTime matchDate;

    private String field;
    private String phase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    @Column(name = "home_goals")
    private int homeGoals;

    @Column(name = "away_goals")
    private int awayGoals;

    private String status;
    private String referee;

    // Cargados vía MatchEventRepository.findByMatchId() — no persisten aquí
    @Transient
    private List<Object> events;

    @Transient
    private List<Object> lineups;

    public void registerResult(int homeGoals, int awayGoals) {}
    public void registerEvent(Object event) {}
    public List<Player> getRedCards() { return null; }
    public List<Player> getYellowCards() { return null; }
    public Team determineWinner() { return null; }
}
