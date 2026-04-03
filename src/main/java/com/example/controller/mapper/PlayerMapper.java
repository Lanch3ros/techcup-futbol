package com.example.controller.mapper;

import com.example.controller.dto.response.ProfileDTO;
import com.example.core.model.Player;
import com.example.core.model.StudentPlayer;
import com.example.core.model.User;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

    public ProfileDTO toDto(Player player) {
        if (player == null) {
            return null;
        }

        User user = (User) player;
        Integer semester = (user instanceof StudentPlayer sp) ? sp.getSemester() : null;

        return new ProfileDTO(
                player.getFullName(),
                player.getEmail(),
                player.getUserType(),
                player.getProfilePhoto(),
                player.getJerseyNumber(),
                player.getPosition(),
                user.getIdentification(),
                user.getGender(),
                user.getBirthDate(),
                user.getProgram(),
                user.getTeamId(),
                semester
        );
    }
}
