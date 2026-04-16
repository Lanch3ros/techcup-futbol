package com.example.controller;

import com.example.controller.dto.request.PaymentRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.core.model.Payment;
import com.example.core.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Pagos", description = "Endpoints para la gestión de pagos e inscripciones de equipos")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    @Operation(summary = "Subir comprobante de pago como archivo (JPG, PNG o PDF, máx. 5 MB)")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericResponse> uploadReceipt(
            @RequestParam("teamId") Long teamId,
            @RequestParam("file") MultipartFile file) {

        log.info("POST /api/v1/payments/upload - equipo ID: {}, archivo: '{}', tamaño: {} bytes",
                teamId, file.getOriginalFilename(), file.getSize());
        try {
            Payment payment = paymentService.uploadReceipt(teamId, file);
            log.info("Comprobante subido exitosamente - pago ID: {}, equipo ID: {}", payment.getId(), teamId);
            return new ResponseEntity<>(new GenericResponse("Éxito", payment), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error al subir comprobante para equipo ID: {} - {}", teamId, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Registrar comprobante de pago")
    @PostMapping
    public ResponseEntity<GenericResponse> createPayment(@RequestBody @Valid PaymentRequest request) {
        log.info("POST /api/v1/payments - equipo ID: {}", request.getTeamId());
        try {
            Payment payment = paymentService.createPayment(request);
            log.info("Comprobante de pago registrado exitosamente - ID: {}, equipo ID: {}", payment.getId(), request.getTeamId());
            return new ResponseEntity<>(new GenericResponse("Éxito", payment), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error al registrar comprobante de pago para equipo ID: {} - {}", request.getTeamId(), e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Listar todos los pagos")
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        log.info("GET /api/v1/payments");
        List<Payment> payments = paymentService.getAllPayments();
        log.info("Total de pagos retornados: {}", payments.size());
        return ResponseEntity.ok(payments);
    }


    @Operation(summary = "Consultar un pago específico")
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        log.info("GET /api/v1/payments/{}", id);
        try {
            Payment payment = paymentService.getPaymentById(id);
            log.info("Pago encontrado - ID: {}, estado: {}", id, payment.getStatus());
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.warn("Pago no encontrado - ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(summary = "Aprobar un pago")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<GenericResponse> approvePayment(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        log.info("PATCH /api/v1/payments/{}/approve", id);
        try {
            String approvedBy = payload.getOrDefault("approvedBy", "Organizador");
            paymentService.approvePayment(id, approvedBy);
            log.info("Pago ID: {} aprobado por: {}", id, approvedBy);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Pago aprobado correctamente. El equipo queda inscrito."));
        } catch (Exception e) {
            log.error("Error al aprobar pago ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Rechazar un pago")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<GenericResponse> rejectPayment(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        log.info("PATCH /api/v1/payments/{}/reject", id);
        try {
            String comments = payload.get("comments");
            if (comments == null || comments.isBlank()) {
                log.warn("Motivo de rechazo no proporcionado para pago ID: {}", id);
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "Debe indicar el motivo del rechazo en 'comments'"));
            }
            paymentService.rejectPayment(id, comments);
            log.info("Pago ID: {} rechazado. Motivo: {}", id, comments);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Pago rechazado. Se notificará al capitán."));
        } catch (Exception e) {
            log.error("Error al rechazar pago ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Enviar pago a revisión (Pendiente → En revisión)")
    @PatchMapping("/{id}/review")
    public ResponseEntity<GenericResponse> sendPaymentToReview(@PathVariable Long id) {
        log.info("PATCH /api/v1/payments/{}/review", id);
        try {
            paymentService.sendPaymentToReview(id);
            log.info("Pago ID: {} enviado a revisión exitosamente", id);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Pago enviado a revisión correctamente."));
        } catch (Exception e) {
            log.error("Error al enviar a revisión pago ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Consultar estado de pago de un equipo")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<Payment> getPaymentByTeam(@PathVariable Long teamId) {
        log.info("GET /api/v1/payments/team/{}", teamId);
        try {
            Payment payment = paymentService.getPaymentByTeam(teamId);
            log.info("Pago encontrado para equipo ID: {}, estado: {}", teamId, payment.getStatus());
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.warn("No se encontró pago para equipo ID: {}", teamId);
            return ResponseEntity.notFound().build();
        }
    }
}