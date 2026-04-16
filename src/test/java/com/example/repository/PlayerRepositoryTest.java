package com.example.repository;

import com.example.core.model.StudentPlayer;
import com.example.core.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PlayerRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
    }

    @Test
    void save_NewPlayer_AssignsId() {
        StudentPlayer player = new StudentPlayer();
        player.setFullName("Jugador Nuevo");

        StudentPlayer savedPlayer = new StudentPlayer();
        savedPlayer.setId(1L);
        savedPlayer.setFullName("Jugador Nuevo");

        when(userRepository.save(any(User.class))).thenReturn(savedPlayer);

        User result = userRepository.save(player);

        assertNotNull(result.getId());
        assertEquals(1L, result.getId());
        assertEquals("Jugador Nuevo", result.getFullName());
    }

    @Test
    void save_ExistingPlayer_UpdatesWithoutChangingId() {
        StudentPlayer player = new StudentPlayer();
        player.setId(5L);
        player.setFullName("Jugador Existente");

        when(userRepository.save(any(User.class))).thenReturn(player);

        User savedPlayer = userRepository.save(player);

        assertEquals(5L, savedPlayer.getId());
        assertEquals("Jugador Existente", savedPlayer.getFullName());
    }

    @Test
    void findAll_ReturnsAllSavedPlayers() {
        when(userRepository.findAll()).thenReturn(List.of(new StudentPlayer(), new StudentPlayer()));

        List<User> players = userRepository.findAll();

        assertEquals(2, players.size());
    }

    @Test
    void findById_ExistingId_ReturnsPlayer() {
        StudentPlayer player = new StudentPlayer();
        player.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(player));

        Optional<User> result = userRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void findById_NonExistingId_ReturnsEmpty() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> result = userRepository.findById(99L);

        assertFalse(result.isPresent());
    }
}
