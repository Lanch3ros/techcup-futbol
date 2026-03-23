package com.example.controller.mapper;

import com.example.controller.dto.request.TeamCreationRequest;
import com.example.core.model.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {

    public Team toEntity(TeamCreationRequest request) {
        if (request == null) {
            return null;
        }
        Team team = new Team();
        team.setName(request.getName());
        team.setColors(request.getColors());
        return team;
    }
}