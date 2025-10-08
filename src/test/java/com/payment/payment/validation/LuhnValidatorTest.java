package com.payment.payment.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LuhnValidatorTest {

    private final LuhnValidator validator = new LuhnValidator();

    @Test
    void whenCardNumberIsValid_thenReturnsTrue() {
        // A known valid credit card number
        assertTrue(validator.isValid("79927398713", null), "A valid card number should return true");
    }

    @Test
    void whenCardNumberIsInvalid_thenReturnsFalse() {
        // An invalid credit card number
        assertFalse(validator.isValid("79927398714", null), "An invalid card number should return false");
    }

    @Test
    void whenCardNumberIsNull_thenReturnsTrue() {
        // Assuming null should be handled by @NotNull and not this validator
        assertTrue(validator.isValid(null, null), "Null should be considered valid by this validator");
    }

    @Test
    void whenCardNumberIsEmpty_thenReturnsTrue() {
        // Assuming empty should be handled by @NotBlank and not this validator
        assertTrue(validator.isValid("", null), "Empty string should be considered valid by this validator");
    }
}