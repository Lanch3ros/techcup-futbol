package com.example.controller;

import com.example.controller.dto.request.PaymentRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.core.model.Payment;
import com.example.core.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("PaymentController – /api/v1/payments")
class PaymentControllerTest {

    private PaymentService paymentService;
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        paymentController = new PaymentController(paymentService);
    }

    // ── uploadReceipt ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("uploadReceipt – JPG válido → 201 CREATED con pago")
    void uploadReceipt_ValidJpg_Returns201() {
        MockMultipartFile file = new MockMultipartFile("file", "receipt.jpg", "image/jpeg", new byte[]{1, 2, 3});
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus("Pendiente");
        when(paymentService.uploadReceipt(eq(1L), any())).thenReturn(payment);

        ResponseEntity<GenericResponse> response = paymentController.uploadReceipt(1L, file);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
        assertEquals(payment, response.getBody().getData());
    }

    @Test
    @DisplayName("uploadReceipt – excepción de servicio → 400 BAD REQUEST")
    void uploadReceipt_ServiceException_Returns400() {
        MockMultipartFile file = new MockMultipartFile("file", "receipt.txt", "text/plain", new byte[]{1});
        when(paymentService.uploadReceipt(eq(1L), any()))
                .thenThrow(new RuntimeException("Formato no permitido"));

        ResponseEntity<GenericResponse> response = paymentController.uploadReceipt(1L, file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Formato no permitido", response.getBody().getData());
    }

    // ── createPayment ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createPayment – éxito → 201 CREATED con pago")
    void createPayment_Success_Returns201() {
        PaymentRequest req = new PaymentRequest();
        req.setTeamId(1L);
        req.setReceiptUrl("http://example.com/receipt.jpg");

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus("Pendiente");
        when(paymentService.createPayment(any())).thenReturn(payment);

        ResponseEntity<GenericResponse> response = paymentController.createPayment(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
        assertEquals(payment, response.getBody().getData());
    }

    @Test
    @DisplayName("createPayment – excepción → 400 BAD REQUEST")
    void createPayment_Exception_Returns400() {
        PaymentRequest req = new PaymentRequest();
        req.setTeamId(99L);
        req.setReceiptUrl("http://example.com/receipt.jpg");
        when(paymentService.createPayment(any()))
                .thenThrow(new RuntimeException("Equipo no encontrado"));

        ResponseEntity<GenericResponse> response = paymentController.createPayment(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Equipo no encontrado", response.getBody().getData());
    }

    // ── getAllPayments ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllPayments → 200 OK con lista completa")
    void getAllPayments_Returns200() {
        when(paymentService.getAllPayments()).thenReturn(List.of(new Payment(), new Payment()));

        ResponseEntity<List<Payment>> response = paymentController.getAllPayments();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    // ── getPaymentById ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getPaymentById – encontrado → 200 OK")
    void getPaymentById_Found_Returns200() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus("Pendiente");
        when(paymentService.getPaymentById(1L)).thenReturn(payment);

        ResponseEntity<Payment> response = paymentController.getPaymentById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pendiente", response.getBody().getStatus());
    }

    @Test
    @DisplayName("getPaymentById – no encontrado → 404 NOT FOUND")
    void getPaymentById_NotFound_Returns404() {
        when(paymentService.getPaymentById(99L)).thenThrow(new RuntimeException("No encontrado"));

        ResponseEntity<Payment> response = paymentController.getPaymentById(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── approvePayment ────────────────────────────────────────────────────────

    @Test
    @DisplayName("approvePayment – éxito con 'approvedBy' en payload → 200 OK")
    void approvePayment_Success_Returns200() {
        Map<String, String> payload = Map.of("approvedBy", "admin@eci.edu.co");
        doNothing().when(paymentService).approvePayment(1L, "admin@eci.edu.co");

        ResponseEntity<GenericResponse> response = paymentController.approvePayment(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("approvePayment – sin 'approvedBy' → usa default 'Organizador'")
    void approvePayment_NoApprovedBy_UsesDefault() {
        Map<String, String> payload = new HashMap<>();
        doNothing().when(paymentService).approvePayment(1L, "Organizador");

        ResponseEntity<GenericResponse> response = paymentController.approvePayment(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paymentService).approvePayment(1L, "Organizador");
    }

    @Test
    @DisplayName("approvePayment – excepción (ya aprobado) → 400 BAD REQUEST")
    void approvePayment_AlreadyApproved_Returns400() {
        Map<String, String> payload = Map.of("approvedBy", "admin");
        doThrow(new RuntimeException("El pago ya fue aprobado"))
                .when(paymentService).approvePayment(1L, "admin");

        ResponseEntity<GenericResponse> response = paymentController.approvePayment(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El pago ya fue aprobado", response.getBody().getData());
    }

    // ── rejectPayment ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("rejectPayment – comments válido → 200 OK")
    void rejectPayment_ValidComments_Returns200() {
        Map<String, String> payload = Map.of("comments", "Comprobante ilegible");
        doNothing().when(paymentService).rejectPayment(1L, "Comprobante ilegible");

        ResponseEntity<GenericResponse> response = paymentController.rejectPayment(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("rejectPayment – comments null → 400 BAD REQUEST")
    void rejectPayment_NullComments_Returns400() {
        Map<String, String> payload = new HashMap<>();
        payload.put("comments", null);

        ResponseEntity<GenericResponse> response = paymentController.rejectPayment(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Debe indicar el motivo del rechazo en 'comments'", response.getBody().getData());
    }

    @Test
    @DisplayName("rejectPayment – comments en blanco → 400 BAD REQUEST")
    void rejectPayment_BlankComments_Returns400() {
        Map<String, String> payload = Map.of("comments", "   ");

        ResponseEntity<GenericResponse> response = paymentController.rejectPayment(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("rejectPayment – excepción de servicio → 400 BAD REQUEST")
    void rejectPayment_ServiceException_Returns400() {
        Map<String, String> payload = Map.of("comments", "Motivo");
        doThrow(new RuntimeException("Pago no encontrado"))
                .when(paymentService).rejectPayment(1L, "Motivo");

        ResponseEntity<GenericResponse> response = paymentController.rejectPayment(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── sendPaymentToReview (GAP-08) ──────────────────────────────────────────

    @Test
    @DisplayName("sendPaymentToReview – éxito → 200 OK")
    void sendPaymentToReview_Success_Returns200() {
        doNothing().when(paymentService).sendPaymentToReview(1L);

        ResponseEntity<GenericResponse> response = paymentController.sendPaymentToReview(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
        verify(paymentService).sendPaymentToReview(1L);
    }

    @Test
    @DisplayName("sendPaymentToReview – excepción → 400 BAD REQUEST")
    void sendPaymentToReview_Exception_Returns400() {
        doThrow(new RuntimeException("Estado no permitido"))
                .when(paymentService).sendPaymentToReview(1L);

        ResponseEntity<GenericResponse> response = paymentController.sendPaymentToReview(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Estado no permitido", response.getBody().getData());
    }

    // ── getPaymentByTeam ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getPaymentByTeam – encontrado → 200 OK con pago")
    void getPaymentByTeam_Found_Returns200() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus("Aprobado");
        when(paymentService.getPaymentByTeam(1L)).thenReturn(payment);

        ResponseEntity<Payment> response = paymentController.getPaymentByTeam(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Aprobado", response.getBody().getStatus());
    }

    @Test
    @DisplayName("getPaymentByTeam – no encontrado → 404 NOT FOUND")
    void getPaymentByTeam_NotFound_Returns404() {
        when(paymentService.getPaymentByTeam(99L))
                .thenThrow(new RuntimeException("Sin pago para el equipo"));

        ResponseEntity<Payment> response = paymentController.getPaymentByTeam(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
