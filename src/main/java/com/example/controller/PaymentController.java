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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    @Operation(summary = "Registrar comprobante de pago",
            description = "El capitán sube el comprobante de pago del equipo. El estado inicial es 'Pendiente'.")
    @PostMapping
    public ResponseEntity<GenericResponse> createPayment(@RequestBody @Valid PaymentRequest request) {
        log.info("POST /api/v1/payments - equipo: {}", request.getTeamId());
        try {
            Payment payment = paymentService.createPayment(request);
            return new ResponseEntity<>(new GenericResponse("Éxito", payment), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Listar todos los pagos",
            description = "Vista del organizador para revisar todos los comprobantes de pago registrados.")
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        log.info("GET /api/v1/payments");
        return ResponseEntity.ok(paymentService.getAllPayments());
    }


    @Operation(summary = "Consultar un pago específico",
            description = "Retorna el detalle de un comprobante de pago por su ID.")
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        log.info("GET /api/v1/payments/{}", id);
        try {
            return ResponseEntity.ok(paymentService.getPaymentById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(summary = "Aprobar un pago",
            description = "El organizador aprueba el comprobante de pago. El equipo queda inscrito en el torneo.")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<GenericResponse> approvePayment(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        log.info("PATCH /api/v1/payments/{}/approve", id);
        try {
            String approvedBy = payload.getOrDefault("approvedBy", "Organizador");
            paymentService.approvePayment(id, approvedBy);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Pago aprobado correctamente. El equipo queda inscrito."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Rechazar un pago",
            description = "El organizador rechaza el comprobante indicando el motivo.")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<GenericResponse> rejectPayment(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        log.info("PATCH /api/v1/payments/{}/reject", id);
        try {
            String comments = payload.get("comments");
            if (comments == null || comments.isBlank()) {
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "Debe indicar el motivo del rechazo en 'comments'"));
            }
            paymentService.rejectPayment(id, comments);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Pago rechazado. Se notificará al capitán."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Consultar estado de pago de un equipo",
            description = "Retorna el comprobante y estado de pago del equipo especificado.")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<Payment> getPaymentByTeam(@PathVariable Long teamId) {
        log.info("GET /api/v1/payments/team/{}", teamId);
        try {
            return ResponseEntity.ok(paymentService.getPaymentByTeam(teamId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}