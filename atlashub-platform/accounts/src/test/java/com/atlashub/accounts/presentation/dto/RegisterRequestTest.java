package com.atlashub.accounts.presentation.dto;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterRequestTest {
    @Test
    void registration_requires_only_identity_fields() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var invalid = validator.validate(new RegisterRequest(
                    "Ada", "Obi", "not-an-email", "password1", "NG"));
            assertTrue(invalid.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));

            var valid = validator.validate(new RegisterRequest(
                    "Ada", "Obi", "ada@example.com", "password1", "NG"));
            assertTrue(valid.isEmpty());
        }
    }
}
