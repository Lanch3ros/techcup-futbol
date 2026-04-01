package com.example.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStats {
    private Long playerId;
    private String playerName;
    private String teamName;
    private int goals;
    private int yellowCards;
    private int redCards;
    private int matchesPlayed;
}