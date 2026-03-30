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
        log.info("Registrando comprobante de pago para equipo ID: {}", request.getTeamId());

        if (teamRepository.findById(request.getTeamId()) == null) {
            log.warn("Equipo no encontrado al registrar pago - ID: {}", request.getTeamId());
            throw new ResourceNotFoundException("Equipo con ID " + request.getTeamId() + " no encontrado");
        }

        Payment payment = new Payment();
        payment.setTeamId(request.getTeamId());
        payment.setStatus("Pendiente");
        payment.setUploadDate(LocalDate.now());
        payment.setReceiptUrl(request.getReceiptUrl());

        Payment saved = paymentRepository.save(payment);
        log.info("Comprobante de pago registrado exitosamente - ID: {}, equipo ID: {}, estado: {}", saved.getId(), request.getTeamId(), saved.getStatus());
        return saved;
    }

    public List<Payment> getAllPayments() {
        log.info("Consultando la lista de todos los pagos");
        List<Payment> payments = paymentRepository.findAll();
        log.info("Total de pagos obtenidos: {}", payments.size());
        return payments;
    }

    public Payment getPaymentById(Long id) {
        log.info("Buscando pago con ID: {}", id);
        Payment payment = paymentRepository.findById(id);
        if (payment == null) {
            log.warn("Pago no encontrado - ID: {}", id);
            throw new ResourceNotFoundException("Pago con ID " + id + " no encontrado");
        }
        log.info("Pago encontrado - ID: {}, estado: {}", id, payment.getStatus());
        return payment;
    }

    public void approvePayment(Long id, String approvedBy) {
        log.info("Aprobando pago ID: {}, aprobado por: {}", id, approvedBy);
        Payment payment = getPaymentById(id);

        if ("Aprobado".equals(payment.getStatus())) {
            log.warn("Pago ID: {} ya fue aprobado anteriormente", id);
            throw new BusinessRuleException("El pago ya fue aprobado anteriormente");
        }

        payment.approve(approvedBy);
        paymentRepository.save(payment);
        log.info("Pago ID: {} aprobado exitosamente por: {}", id, approvedBy);
    }

    public void rejectPayment(Long id, String comments) {
        log.info("Rechazando pago ID: {}", id);
        Payment payment = getPaymentById(id);
        payment.reject(comments);
        paymentRepository.save(payment);
        log.info("Pago ID: {} rechazado exitosamente. Motivo: {}", id, comments);
    }

    public Payment getPaymentByTeam(Long teamId) {
        log.info("Buscando pago del equipo ID: {}", teamId);
        return paymentRepository.findAll().stream()
                .filter(p -> p.getTeamId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("No se encontró pago para el equipo ID: {}", teamId);
                    return new ResourceNotFoundException("No se encontró pago para el equipo con ID " + teamId);
                });
    }
}