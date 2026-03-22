package com.example.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Referee {
    private String licenseNumber;
    private String certificationLevel;
    private List<Match> assignedMatches;

    public void registerResult(Match match) {}
    public void registerEvent(Object event) {}
    public void issueCard(Player player, String type) {}
    public List<Match> getAssignedMatches() { return null; }
}