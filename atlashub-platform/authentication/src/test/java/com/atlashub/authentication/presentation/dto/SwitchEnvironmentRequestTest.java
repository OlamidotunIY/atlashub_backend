package com.atlashub.authentication.presentation.dto;

import com.atlashub.shared.application.security.ApiEnvironment;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitchEnvironmentRequestTest {
    @Test
    void environment_is_required() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertTrue(validator.validate(new SwitchEnvironmentRequest(null)).stream()
                    .anyMatch(value -> value.getPropertyPath().toString().equals("environment")));
            assertTrue(validator.validate(new SwitchEnvironmentRequest(ApiEnvironment.LIVE)).isEmpty());
        }
    }

    @Test
    void login_request_requires_environment() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var violations = factory.getValidator().validate(
                    new LoginRequest("a@b.com", "secret", null, "fp"));
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(value -> value.getPropertyPath().toString().equals("environment")));
        }
    }
}
