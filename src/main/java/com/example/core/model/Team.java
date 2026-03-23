package com.example.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {
    private Long id;
    private String name;
    private String colors;
    private String paymentStatus;
    private List<Player> players;
    private Player captain;
    private Long tournamentId;
    private int matchesPlayed;
    private int matchesWon;
    private int matchesDrawn;
    private int matchesLost;
    private int goalsFor;
    private int goalsAgainst;
    private int goalDifference;
    private int points;

    public void addPlayer(Player player) {}
    public void removePlayer(Long playerId) {}
    public boolean validateCapacity() { return false; }
    public List<Player> getAvailablePlayers() { return null; }
}