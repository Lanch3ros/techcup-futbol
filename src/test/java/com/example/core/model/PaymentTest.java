package com.example.core.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    @Test
    void testLombokMethods() {
        LocalDate date = LocalDate.now();
        Payment payment = new Payment(1L, "PENDING", date, date, "Admin", "Sin comentarios");

        assertEquals(1L, payment.getId());
        assertEquals("PENDING", payment.getStatus());
        assertEquals(date, payment.getUploadDate());
        assertEquals(date, payment.getReviewDate());
        assertEquals("Admin", payment.getApprovedBy());
        assertEquals("Sin comentarios", payment.getComments());

        Payment payment2 = new Payment();
        payment2.setId(1L);
        payment2.setStatus("PENDING");
        payment2.setUploadDate(date);
        payment2.setReviewDate(date);
        payment2.setApprovedBy("Admin");
        payment2.setComments("Sin comentarios");

        assertEquals(payment, payment2);
        assertEquals(payment.hashCode(), payment2.hashCode());
        assertNotNull(payment.toString());
    }

    @Test
    void testCustomMethods() {
        Payment payment = new Payment();

        assertFalse(payment.isApproved());

        payment.approve("Organizador");
        payment.reject("Comprobante borroso");
        payment.sendToReview();
    }
}