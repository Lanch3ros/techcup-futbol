package com.example.core.validator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GmailValidatorTest {

    private final GmailValidator validator = new GmailValidator();

    @Test
    void validate_ShouldReturnTrue_WhenEmailHasCorrectDomain() {
        boolean result = validator.validate("usuario@gmail.com");
        assertTrue(result);
    }

    @Test
    void validate_ShouldReturnTrue_WhenEmailHasMixedCase() {
        boolean result = validator.validate("UsUaRiO@GmAiL.cOm");
        assertTrue(result);
    }

    @Test
    void validate_ShouldReturnFalse_WhenEmailIsNull() {
        boolean result = validator.validate(null);
        assertFalse(result);
    }

    @Test
    void validate_ShouldReturnFalse_WhenEmailIsEmpty() {
        boolean result = validator.validate("");
        assertFalse(result);
    }

    @Test
    void validate_ShouldReturnFalse_WhenEmailHasIncorrectDomain() {
        boolean result = validator.validate("usuario@hotmail.com");
        assertFalse(result);
    }

    @Test
    void getDomain_ShouldReturnCorrectDomain() {
        assertEquals("@gmail.com", validator.getDomain());
    }
}