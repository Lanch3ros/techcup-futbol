package com.example.controller.mapper;

import com.example.controller.dto.request.TournamentCreationRequest;
import com.example.core.model.Tournament;
import org.springframework.stereotype.Component;

@Component
public class TournamentMapper {

    public Tournament toEntity(TournamentCreationRequest request) {
        if (request == null) {
            return null;
        }

        Tournament tournament = new Tournament();
        tournament.setStartDate(request.getStartDate());
        tournament.setEndDate(request.getEndDate());
        tournament.setTeamCost(request.getTeamCost());

        tournament.setRegulations(request.getRules());

        tournament.setStatus("CREATED");

        return tournament;
    }
}