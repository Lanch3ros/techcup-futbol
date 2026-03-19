package com.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    private Long id;
    private LocalDateTime matchDate;
    private String field;
    private Team homeTeam;
    private Team awayTeam;
    private int homeGoals;
    private int awayGoals;
    private String status;
    private String referee;
    private List<Object> events;
    private List<Object> lineups;

    public void registerResult(int homeGoals, int awayGoals) {}
    public void registerEvent(Object event) {}
    public List<Player> getRedCards() { return null; }
    public List<Player> getYellowCards() { return null; }
    public Team determineWinner() { return null; }
}