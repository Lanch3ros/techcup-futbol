package com.example.core.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    @Test
    void testLombokMethods() {
        LocalDate date = LocalDate.now();
        Payment payment = new Payment(1L, 10L, "PENDING", date, date, "Admin", "url/recibo.jpg", "Sin comentarios");

        assertEquals(1L, payment.getId());
        assertEquals(10L, payment.getTeamId());
        assertEquals("PENDING", payment.getStatus());
        assertEquals(date, payment.getUploadDate());
        assertEquals(date, payment.getReviewDate());
        assertEquals("Admin", payment.getApprovedBy());
        assertEquals("url/recibo.jpg", payment.getReceiptUrl());
        assertEquals("Sin comentarios", payment.getComments());

        Payment payment2 = new Payment();
        payment2.setId(1L);
        payment2.setTeamId(10L);
        payment2.setStatus("PENDING");
        payment2.setUploadDate(date);
        payment2.setReviewDate(date);
        payment2.setApprovedBy("Admin");
        payment2.setReceiptUrl("url/recibo.jpg");
        payment2.setComments("Sin comentarios");

        assertEquals(payment, payment2);
        assertEquals(payment.hashCode(), payment2.hashCode());
        assertNotNull(payment.toString());
    }

    @Test
    void testApprove_ChangesStatusToAprobado() {
        Payment payment = new Payment();
        assertFalse(payment.isApproved());

        payment.approve("Organizador");

        assertEquals("Aprobado", payment.getStatus());
        assertEquals("Organizador", payment.getApprovedBy());
        assertNotNull(payment.getReviewDate());
        assertTrue(payment.isApproved());
    }

    @Test
    void testReject_ChangesStatusToRechazado() {
        Payment payment = new Payment();

        payment.reject("Comprobante borroso");

        assertEquals("Rechazado", payment.getStatus());
        assertEquals("Comprobante borroso", payment.getComments());
        assertNotNull(payment.getReviewDate());
    }

    @Test
    void testSendToReview_ChangesStatusToEnRevision() {
        Payment payment = new Payment();

        payment.sendToReview();

        assertEquals("En revisión", payment.getStatus());
    }
}