package com.example.core.validator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StudentEmailValidatorTest {

    private final StudentEmailValidator validator = new StudentEmailValidator();

    @Test
    void validate_ShouldReturnTrue_WhenEmailHasCorrectDomain() {
        boolean result = validator.validate("estudiante@mail.escuelaing.edu.co");
        assertTrue(result);
    }

    @Test
    void validate_ShouldReturnTrue_WhenEmailHasMixedCase() {
        boolean result = validator.validate("EsTuDiAnTe@MaIl.EsCuElAiNg.EdU.cO");
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
        boolean result = validator.validate("estudiante@escuelaing.edu.co");
        assertFalse(result);
    }

    @Test
    void getDomain_ShouldReturnCorrectDomain() {
        assertEquals("@mail.escuelaing.edu.co", validator.getDomain());
    }
}