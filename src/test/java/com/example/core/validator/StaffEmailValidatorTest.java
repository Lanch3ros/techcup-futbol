package com.example.core.validator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StaffEmailValidatorTest {

    private final StaffEmailValidator validator = new StaffEmailValidator();

    @Test
    void validate_ShouldReturnTrue_WhenEmailHasCorrectDomain() {
        assertTrue(validator.validate("staff@escuelaing.edu.co"));
    }

    @Test
    void validate_ShouldReturnTrue_WhenEmailHasMixedCase() {
        assertTrue(validator.validate("StAfF@EsCuElAiNg.EdU.cO"));
    }

    @Test
    void validate_ShouldReturnFalse_WhenEmailIsNull() {
        assertFalse(validator.validate(null));
    }

    @Test
    void validate_ShouldReturnFalse_WhenEmailIsEmpty() {
        assertFalse(validator.validate(""));
    }

    @Test
    void validate_ShouldReturnFalse_WhenEmailHasIncorrectDomain() {
        assertFalse(validator.validate("staff@gmail.com"));
    }

    @Test
    void validate_ShouldReturnFalse_WhenDomainIsStudentSubdomain() {
        assertFalse(validator.validate("staff@mail.escuelaing.edu.co"));
    }

    @Test
    void getDomain_ShouldReturnCorrectDomain() {
        assertEquals("@escuelaing.edu.co", validator.getDomain());
    }
}
