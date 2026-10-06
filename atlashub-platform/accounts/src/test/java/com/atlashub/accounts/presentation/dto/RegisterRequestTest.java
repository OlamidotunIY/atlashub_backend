package com.atlashub.accounts.presentation.dto;

import com.atlashub.accounts.domain.valueobject.AtlasHubRegistrationType;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterRequestTest {
    @Test
    void registration_type_and_industry_are_required() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var invalid = validator.validate(new RegisterRequest(
                    "Ada", "Obi", "ada@example.com", "password1", "NG", "Acme",
                    null, null, null, null, null));
            assertTrue(invalid.stream().anyMatch(v -> v.getPropertyPath().toString().equals("registrationType")));
            assertTrue(invalid.stream().anyMatch(v -> v.getPropertyPath().toString().equals("industry")));

            var valid = validator.validate(new RegisterRequest(
                    "Ada", "Obi", "ada@example.com", "password1", "NG", "Acme",
                    AtlasHubRegistrationType.SOLE_PROPRIETORSHIP, SupportedIndustry.RETAIL,
                    null, null, null));
            assertTrue(valid.isEmpty());
        }
    }
}
