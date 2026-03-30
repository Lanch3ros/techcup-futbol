package com.example.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchEvent {
    private Long id;
    private Long matchId;
    private Long playerId;
    private String playerName;
    private String type;
    private int minute;
}