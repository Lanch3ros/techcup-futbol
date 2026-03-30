package com.example.repository;

import com.example.core.model.Referee;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class RefereeRepository {

    private final Map<Long, Referee> refereeDB = new HashMap<>();
    private long currentId = 1;

    public Referee save(Referee referee) {
        if (referee.getLicenseNumber() == null) {
            referee.setLicenseNumber(String.valueOf(currentId++));
        }
        refereeDB.put(Long.parseLong(referee.getLicenseNumber()), referee);
        return referee;
    }

    public List<Referee> findAll() {
        return new ArrayList<>(refereeDB.values());
    }

    public Referee findById(Long id) {
        return refereeDB.get(id);
    }
}