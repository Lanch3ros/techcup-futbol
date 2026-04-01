package com.example.core.service;

import com.example.controller.dto.request.PaymentRequest;
import com.example.core.exception.BusinessRuleException;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.Payment;
import com.example.repository.PaymentRepository;
import com.example.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class PaymentService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "application/pdf"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".pdf"
    );

    private final PaymentRepository paymentRepository;
    private final TeamRepository teamRepository;

    @Value("${app.uploads.dir:uploads}")
    private String uploadDir;

    public PaymentService(PaymentRepository paymentRepository, TeamRepository teamRepository) {
        this.paymentRepository = paymentRepository;
        this.teamRepository = teamRepository;
    }

    public Payment uploadReceipt(Long teamId, MultipartFile file) {
        log.info("Subiendo comprobante de pago para equipo ID: {}, archivo: '{}'", teamId, file.getOriginalFilename());

        if (teamRepository.findById(teamId) == null) {
            throw new ResourceNotFoundException("Equipo con ID " + teamId + " no encontrado");
        }

        if (file.isEmpty()) {
            throw new BusinessRuleException("El archivo no puede estar vacío.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            log.warn("Tipo de contenido rechazado '{}' para equipo ID: {}", contentType, teamId);
            throw new BusinessRuleException("Tipo de archivo no permitido. Solo se aceptan JPG, PNG o PDF.");
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "comprobante";
        String extension = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase()
                : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("Extensión rechazada '{}' para equipo ID: {}", extension, teamId);
            throw new BusinessRuleException("Extensión no permitida. Solo se aceptan: .jpg, .jpeg, .png, .pdf");
        }

        try {
            Path uploadPath = Paths.get(uploadDir, "payments");
            Files.createDirectories(uploadPath);

            String savedFilename = teamId + "_" + System.currentTimeMillis() + extension;
            Path destination = uploadPath.resolve(savedFilename);
            Files.copy(file.getInputStream(), destination);

            Payment payment = new Payment();
            payment.setTeamId(teamId);
            payment.setStatus("Pendiente");
            payment.setUploadDate(LocalDate.now());
            payment.setReceiptUrl(destination.toString());

            Payment saved = paymentRepository.save(payment);
            log.info("Comprobante guardado exitosamente - ID: {}, ruta: '{}'", saved.getId(), destination);
            return saved;

        } catch (IOException e) {
            log.error("Error al guardar el archivo para equipo ID: {} - {}", teamId, e.getMessage());
            throw new BusinessRuleException("Error al guardar el archivo. Intente nuevamente.");
        }
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