package com.example.core.service;

import com.example.controller.dto.request.PaymentRequest;
import com.example.core.exception.BusinessRuleException;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.Payment;
import com.example.repository.PaymentRepository;
import com.example.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TeamRepository teamRepository;

    public PaymentService(PaymentRepository paymentRepository, TeamRepository teamRepository) {
        this.paymentRepository = paymentRepository;
        this.teamRepository = teamRepository;
    }

    public Payment createPayment(PaymentRequest request) {
        log.info("Registrando comprobante de pago para equipo {}", request.getTeamId());

        if (teamRepository.findById(request.getTeamId()) == null) {
            throw new ResourceNotFoundException("Equipo con ID " + request.getTeamId() + " no encontrado");
        }

        Payment payment = new Payment();
        payment.setTeamId(request.getTeamId());
        payment.setStatus("Pendiente");
        payment.setUploadDate(LocalDate.now());
        payment.setReceiptUrl(request.getReceiptUrl());

        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id);
        if (payment == null) throw new ResourceNotFoundException("Pago con ID " + id + " no encontrado");
        return payment;
    }

    public void approvePayment(Long id, String approvedBy) {
        log.info("Aprobando pago {} por {}", id, approvedBy);
        Payment payment = getPaymentById(id);
        if ("Aprobado".equals(payment.getStatus())) {
            throw new BusinessRuleException("El pago ya fue aprobado anteriormente");
        }
        payment.approve(approvedBy);
        paymentRepository.save(payment);
    }

    public void rejectPayment(Long id, String comments) {
        log.info("Rechazando pago {} con comentario: {}", id, comments);
        Payment payment = getPaymentById(id);
        payment.reject(comments);
        paymentRepository.save(payment);
    }

    public Payment getPaymentByTeam(Long teamId) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getTeamId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró pago para el equipo con ID " + teamId));
    }
}