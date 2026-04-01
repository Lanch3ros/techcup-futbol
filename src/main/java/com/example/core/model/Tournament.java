package com.example.core.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tournaments")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "team_cost")
    private Double teamCost;

    private String status;
    private String regulations;

    @Column(name = "max_teams")
    private int maxTeams;

    @ElementCollection
    @CollectionTable(name = "tournament_fields", joinColumns = @JoinColumn(name = "tournament_id"))
    @Column(name = "field")
    private List<String> fields;

    @ManyToMany
    @JoinTable(
        name = "tournament_teams",
        joinColumns = @JoinColumn(name = "tournament_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private List<Team> registeredTeams;

    @OneToMany
    @JoinColumn(name = "tournament_id")
    private List<Match> matches;

    public void registerTeam(Team team) {}
    public void closeRegistration() {}
    public void generateMatches() {}
    public List<Match> getCalendar() { return null; }
    public boolean validateDates() { return false; }
}
