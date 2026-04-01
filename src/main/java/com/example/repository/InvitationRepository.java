package com.example.repository;

import com.example.core.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    List<Invitation> findByPlayerIdAndStatus(Long playerId, String status);

    boolean existsByPlayerIdAndTeamIdAndStatus(Long playerId, Long teamId, String status);
}
