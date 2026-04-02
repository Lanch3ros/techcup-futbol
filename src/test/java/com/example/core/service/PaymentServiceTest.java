package com.example.core.service;

import com.example.controller.dto.request.PaymentRequest;
import com.example.core.exception.BusinessRuleException;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.Payment;
import com.example.repository.PaymentRepository;
import com.example.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("PaymentService – Gestión de pagos")
class PaymentServiceTest {

    @TempDir
    Path tempDir;

    private PaymentRepository paymentRepository;
    private TeamRepository teamRepository;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        teamRepository    = mock(TeamRepository.class);
        paymentService    = new PaymentService(paymentRepository, teamRepository);
        ReflectionTestUtils.setField(paymentService, "uploadDir", tempDir.toString());
    }

    // ── uploadReceipt – validaciones ──────────────────────────────────────────

    @Test
    @DisplayName("uploadReceipt – equipo no encontrado → ResourceNotFoundException")
    void uploadReceipt_TeamNotFound() {
        when(teamRepository.existsById(99L)).thenReturn(false);
        MockMultipartFile file = new MockMultipartFile("file", "r.jpg", "image/jpeg", new byte[]{1});

        assertThrows(ResourceNotFoundException.class, () -> paymentService.uploadReceipt(99L, file));
    }

    @Test
    @DisplayName("uploadReceipt – archivo vacío → BusinessRuleException")
    void uploadReceipt_EmptyFile() {
        when(teamRepository.existsById(1L)).thenReturn(true);
        MockMultipartFile file = new MockMultipartFile("file", "r.jpg", "image/jpeg", new byte[0]);

        assertThrows(BusinessRuleException.class, () -> paymentService.uploadReceipt(1L, file));
    }

    @Test
    @DisplayName("uploadReceipt – content-type inválido → BusinessRuleException")
    void uploadReceipt_InvalidContentType() {
        when(teamRepository.existsById(1L)).thenReturn(true);
        MockMultipartFile file = new MockMultipartFile("file", "r.txt", "text/plain", new byte[]{1});

        assertThrows(BusinessRuleException.class, () -> paymentService.uploadReceipt(1L, file));
    }

    @Test
    @DisplayName("uploadReceipt – extensión inválida pese a content-type válido → BusinessRuleException")
    void uploadReceipt_InvalidExtension() {
        when(teamRepository.existsById(1L)).thenReturn(true);
        MockMultipartFile file = new MockMultipartFile("file", "r.bmp", "image/jpeg", new byte[]{1});

        assertThrows(BusinessRuleException.class, () -> paymentService.uploadReceipt(1L, file));
    }

    @Test
    @DisplayName("uploadReceipt – nombre sin extensión → BusinessRuleException")
    void uploadReceipt_NoExtension() {
        when(teamRepository.existsById(1L)).thenReturn(true);
        MockMultipartFile file = new MockMultipartFile("file", "receipt", "image/jpeg", new byte[]{1});

        assertThrows(BusinessRuleException.class, () -> paymentService.uploadReceipt(1L, file));
    }

    @Test
    @DisplayName("uploadReceipt – JPG válido → Payment con estado Pendiente")
    void uploadReceipt_Success_Jpg() {
        when(teamRepository.existsById(1L)).thenReturn(true);
        MockMultipartFile file = new MockMultipartFile("file", "receipt.jpg", "image/jpeg", new byte[]{1, 2, 3});

        Payment saved = new Payment(); saved.setId(1L); saved.setStatus("Pendiente");
        when(paymentRepository.save(any())).thenReturn(saved);

        Payment result = paymentService.uploadReceipt(1L, file);
        assertNotNull(result);
        assertEquals("Pendiente", result.getStatus());
    }

    @Test
    @DisplayName("uploadReceipt – PNG válido → Payment persistido")
    void uploadReceipt_Success_Png() {
        when(teamRepository.existsById(1L)).thenReturn(true);
        MockMultipartFile file = new MockMultipartFile("file", "receipt.png", "image/png", new byte[]{1, 2, 3});

        Payment saved = new Payment(); saved.setId(2L); saved.setStatus("Pendiente");
        when(paymentRepository.save(any())).thenReturn(saved);

        assertNotNull(paymentService.uploadReceipt(1L, file));
    }

    @Test
    @DisplayName("uploadReceipt – contentType null → BusinessRuleException (rama null)")
    void uploadReceipt_NullContentType_Throws() throws IOException {
        when(teamRepository.existsById(1L)).thenReturn(true);

        MultipartFile nullTypeFile = mock(MultipartFile.class);
        when(nullTypeFile.isEmpty()).thenReturn(false);
        when(nullTypeFile.getContentType()).thenReturn(null); // rama contentType == null
        when(nullTypeFile.getOriginalFilename()).thenReturn("r.jpg");

        assertThrows(BusinessRuleException.class, () -> paymentService.uploadReceipt(1L, nullTypeFile));
    }

    @Test
    @DisplayName("uploadReceipt – originalFilename null → usa 'comprobante' como nombre base")
    void uploadReceipt_NullOriginalFilename_UsesDefault() throws IOException {
        when(teamRepository.existsById(1L)).thenReturn(true);

        MultipartFile nullNameFile = mock(MultipartFile.class);
        when(nullNameFile.isEmpty()).thenReturn(false);
        when(nullNameFile.getContentType()).thenReturn("image/jpeg");
        when(nullNameFile.getOriginalFilename()).thenReturn(null); // rama getOriginalFilename() == null
        // sin extensión → BusinessRuleException (extensión vacía "")
        assertThrows(BusinessRuleException.class, () -> paymentService.uploadReceipt(1L, nullNameFile));
    }

    @Test
    @DisplayName("uploadReceipt – IOException al copiar archivo → BusinessRuleException")
    void uploadReceipt_IOException_Throws() throws IOException {
        when(teamRepository.existsById(1L)).thenReturn(true);

        MultipartFile brokenFile = mock(MultipartFile.class);
        when(brokenFile.isEmpty()).thenReturn(false);
        when(brokenFile.getContentType()).thenReturn("image/jpeg");
        when(brokenFile.getOriginalFilename()).thenReturn("receipt.jpg");
        when(brokenFile.getInputStream()).thenThrow(new IOException("Disk full"));

        assertThrows(BusinessRuleException.class, () -> paymentService.uploadReceipt(1L, brokenFile));
    }

    @Test
    @DisplayName("uploadReceipt – PDF válido → Payment persistido")
    void uploadReceipt_Success_Pdf() {
        when(teamRepository.existsById(1L)).thenReturn(true);
        MockMultipartFile file = new MockMultipartFile("file", "receipt.pdf", "application/pdf", new byte[]{1, 2, 3});

        Payment saved = new Payment(); saved.setId(3L); saved.setStatus("Pendiente");
        when(paymentRepository.save(any())).thenReturn(saved);

        assertNotNull(paymentService.uploadReceipt(1L, file));
    }

    // ── createPayment ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createPayment – equipo no encontrado → ResourceNotFoundException")
    void createPayment_TeamNotFound() {
        PaymentRequest req = new PaymentRequest();
        req.setTeamId(99L); req.setReceiptUrl("http://x.com/r.jpg");
        when(teamRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> paymentService.createPayment(req));
    }

    @Test
    @DisplayName("createPayment – éxito → Payment con estado Pendiente")
    void createPayment_Success() {
        PaymentRequest req = new PaymentRequest();
        req.setTeamId(1L); req.setReceiptUrl("http://x.com/r.jpg");
        when(teamRepository.existsById(1L)).thenReturn(true);

        Payment saved = new Payment(); saved.setId(1L); saved.setStatus("Pendiente");
        when(paymentRepository.save(any())).thenReturn(saved);

        Payment result = paymentService.createPayment(req);
        assertNotNull(result);
        assertEquals("Pendiente", result.getStatus());
    }

    // ── getAllPayments ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllPayments – devuelve lista completa")
    void getAllPayments_ReturnsList() {
        when(paymentRepository.findAll()).thenReturn(List.of(new Payment(), new Payment()));
        assertEquals(2, paymentService.getAllPayments().size());
    }

    // ── getPaymentById ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getPaymentById – encontrado")
    void getPaymentById_Found() {
        Payment p = new Payment(); p.setId(1L); p.setStatus("Pendiente");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(p));

        assertEquals(1L, paymentService.getPaymentById(1L).getId());
    }

    @Test
    @DisplayName("getPaymentById – no encontrado → ResourceNotFoundException")
    void getPaymentById_NotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentById(99L));
    }

    // ── approvePayment ────────────────────────────────────────────────────────

    @Test
    @DisplayName("approvePayment – ya aprobado → BusinessRuleException")
    void approvePayment_AlreadyApproved_Throws() {
        Payment p = new Payment(); p.setId(1L); p.setStatus("Aprobado");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(BusinessRuleException.class, () -> paymentService.approvePayment(1L, "admin"));
    }

    @Test
    @DisplayName("approvePayment – case-insensitive 'APROBADO' → también rechaza")
    void approvePayment_AlreadyApproved_CaseInsensitive() {
        Payment p = new Payment(); p.setId(1L); p.setStatus("APROBADO");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(BusinessRuleException.class, () -> paymentService.approvePayment(1L, "admin"));
    }

    @Test
    @DisplayName("approvePayment – pendiente → se aprueba y persiste")
    void approvePayment_Success() {
        Payment p = new Payment(); p.setId(1L); p.setStatus("Pendiente");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(p));
        when(paymentRepository.save(any())).thenReturn(p);

        assertDoesNotThrow(() -> paymentService.approvePayment(1L, "admin@eci.edu.co"));
        verify(paymentRepository).save(p);
    }

    // ── rejectPayment ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("rejectPayment – rechaza correctamente y persiste")
    void rejectPayment_Success() {
        Payment p = new Payment(); p.setId(1L); p.setStatus("Pendiente");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(p));
        when(paymentRepository.save(any())).thenReturn(p);

        assertDoesNotThrow(() -> paymentService.rejectPayment(1L, "Comprobante ilegible"));
        verify(paymentRepository).save(p);
    }

    // ── getPaymentByTeam ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getPaymentByTeam – equipo con pago → devuelve Payment")
    void getPaymentByTeam_Found() {
        Payment p = new Payment(); p.setId(1L);
        when(paymentRepository.findFirstByTeamId(1L)).thenReturn(Optional.of(p));
        assertNotNull(paymentService.getPaymentByTeam(1L));
    }

    @Test
    @DisplayName("getPaymentByTeam – sin pago → ResourceNotFoundException")
    void getPaymentByTeam_NotFound() {
        when(paymentRepository.findFirstByTeamId(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentByTeam(99L));
    }
}
