package com.example.repository;

import com.example.core.model.Player;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PlayerRepository {

    private final Map<Long, Player> playersDB = new HashMap<>();
    private long currentId = 1;

    public Player save(Player player) {
        if (player.getId() == null) {
            player.setId(currentId++);
        }
        playersDB.put(player.getId(), player);
        return player;
    }

    public List<Player> findAll() {
        return new ArrayList<>(playersDB.values());
    }

    public Player findById(Long id) {
        return playersDB.get(id);
    }
}
