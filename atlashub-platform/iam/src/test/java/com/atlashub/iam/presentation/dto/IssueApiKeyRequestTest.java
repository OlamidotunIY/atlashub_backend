package com.atlashub.iam.presentation.dto;

import com.atlashub.shared.application.security.ApiEnvironment;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IssueApiKeyRequestTest {
    @Test
    void environment_and_role_are_required() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var violations = factory.getValidator().validate(new IssueApiKeyRequest("Server", null, null));
            assertTrue(violations.stream().anyMatch(value -> value.getPropertyPath().toString().equals("environment")));
            assertTrue(violations.stream().anyMatch(value -> value.getPropertyPath().toString().equals("boundRoleId")));
        }
    }

    @Test
    void accepts_a_typed_environment() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var violations = factory.getValidator()
                    .validate(new IssueApiKeyRequest("Server", ApiEnvironment.TEST, 4L));
            assertTrue(violations.isEmpty());
        }
    }
}
