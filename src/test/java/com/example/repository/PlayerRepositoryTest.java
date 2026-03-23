package com.example.repository;

import com.example.core.model.Player;
import com.example.core.model.StudentPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerRepositoryTest {

    private PlayerRepository playerRepository;

    @BeforeEach
    void setUp() {
        playerRepository = new PlayerRepository();
    }

    @Test
    void save_NewPlayer_AssignsId() {
        Player player = new StudentPlayer();
        player.setFullName("Jugador Nuevo");

        Player savedPlayer = playerRepository.save(player);

        assertNotNull(savedPlayer.getId());
        assertEquals(1L, savedPlayer.getId());
        assertEquals("Jugador Nuevo", savedPlayer.getFullName());
    }

    @Test
    void save_ExistingPlayer_UpdatesWithoutChangingId() {
        Player player = new StudentPlayer();
        player.setId(5L);
        player.setFullName("Jugador Existente");

        Player savedPlayer = playerRepository.save(player);

        assertEquals(5L, savedPlayer.getId());
        assertEquals("Jugador Existente", savedPlayer.getFullName());
    }

    @Test
    void findAll_ReturnsAllSavedPlayers() {
        playerRepository.save(new StudentPlayer());
        playerRepository.save(new StudentPlayer());

        List<Player> players = playerRepository.findAll();

        assertEquals(2, players.size());
    }

    @Test
    void findById_ExistingId_ReturnsPlayer() {
        Player player = new StudentPlayer();
        playerRepository.save(player);

        Player foundPlayer = playerRepository.findById(1L);

        assertNotNull(foundPlayer);
        assertEquals(1L, foundPlayer.getId());
    }

    @Test
    void findById_NonExistingId_ReturnsNull() {
        Player foundPlayer = playerRepository.findById(99L);

        assertNull(foundPlayer);
    }
}