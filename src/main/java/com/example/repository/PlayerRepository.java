package com.example.repository;

import com.example.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByTeamId(Long teamId);

    long countByTeamId(Long teamId);

    List<User> findByPositionAndAvailableTrue(String position);

    List<User> findByFullNameContainingIgnoreCase(String name);
}
