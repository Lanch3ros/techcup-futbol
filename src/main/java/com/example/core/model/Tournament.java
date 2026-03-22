package com.example.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double teamCost;
    private String status;
    private String regulations;
    private int maxTeams;
    private List<String> fields;
    private List<Team> registeredTeams;
    private List<Match> matches;

    public void registerTeam(Team team) {}
    public void closeRegistration() {}
    public void generateMatches() {}
    public List<Match> getCalendar() { return null; }
    public boolean validateDates() { return false; }
}