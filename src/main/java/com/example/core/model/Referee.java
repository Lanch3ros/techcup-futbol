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
@Table(name = "referees")
public class Referee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    private String email;

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    @Column(name = "certification_level")
    private String certificationLevel;

    @ElementCollection
    @CollectionTable(name = "referee_matches", joinColumns = @JoinColumn(name = "referee_id"))
    @Column(name = "match_id")
    private List<Long> assignedMatchIds = new ArrayList<>();

    public void registerResult(Match match) {}
    public void registerEvent(Object event) {}
    public void issueCard(Player player, String type) {}
}
