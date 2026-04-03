package com.example.repository;

import com.example.core.model.RefereeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefereeRepository extends JpaRepository<RefereeUser, Long> {
}
