package com.example.core.validator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdminValidatorTest {

    private final AdminValidator validator = new AdminValidator();

    @Test
    void validate_ShouldReturnTrue_WhenEmailHasCorrectDomain() {
        boolean result = validator.validate("director@escuelaing.edu.co");
        assertTrue(result);
    }

    @Test
    void validate_ShouldReturnTrue_WhenEmailHasMixedCase() {
        boolean result = validator.validate("DiReCtOr@EsCuElAiNg.EdU.cO");
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
        boolean result = validator.validate("director@gmail.com");
        assertFalse(result);
    }

    @Test
    void validate_ShouldReturnFalse_WhenDomainIsPartial() {
        boolean result = validator.validate("director@mail.escuelaing.edu.co");
        assertFalse(result);
    }

    @Test
    void getDomain_ShouldReturnCorrectDomain() {
        assertEquals("@escuelaing.edu.co", validator.getDomain());
    }
}