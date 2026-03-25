package com.example.controller.mapper;

import com.example.controller.dto.response.ProfileDTO;
import com.example.core.model.Player;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

    public ProfileDTO toDto(Player player) {
        if (player == null) {
            return null;
        }

        return new ProfileDTO(
                player.getFullName(),
                player.getEmail(),
                player.getUserType(),
                player.getProfilePhoto(),
                null,
                null
        );
    }
}