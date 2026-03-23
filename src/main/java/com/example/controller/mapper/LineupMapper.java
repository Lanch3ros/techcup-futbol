package com.example.controller.mapper;

import com.example.controller.dto.request.LineupRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LineupMapper {

    public Map<String, Object> toEntity(LineupRequest request) {
        if (request == null) {
            return null;
        }

        Map<String, Object> lineupEntity = new HashMap<>();
        lineupEntity.put("startingPlayersIds", request.getStartingPlayersIds());
        lineupEntity.put("formation", request.getFormation());

        return lineupEntity;
    }
}