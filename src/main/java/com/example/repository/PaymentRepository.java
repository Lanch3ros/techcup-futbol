package com.example.repository;

import com.example.core.model.Payment;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PaymentRepository {

    private final Map<Long, Payment> paymentDB = new HashMap<>();
    private long currentId = 1;

    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            payment.setId(currentId++);
        }
        paymentDB.put(payment.getId(), payment);
        return payment;
    }

    public List<Payment> findAll() {
        return new ArrayList<>(paymentDB.values());
    }

    public Payment findById(Long id) {
        return paymentDB.get(id);
    }
}