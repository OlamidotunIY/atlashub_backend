package com.atlashub.compliance.presentation.dto;

import com.atlashub.compliance.domain.valueobject.OfficerRole;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BusinessOfficerRequestTest {
    @Test
    void role_is_required_and_typed() {
        AddressRequest address = new AddressRequest("1 Main St", null, "Lagos", "Lagos", "100001", "NG");
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var missing = validator.validate(request(null, address));
            assertTrue(missing.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
            assertTrue(validator.validate(request(OfficerRole.OWNER, address)).isEmpty());
        }
    }

    private BusinessOfficerRequest request(OfficerRole role, AddressRequest address) {
        return new BusinessOfficerRequest(role, "Ada", null, "Obi", null, "NG", LocalDate.of(1990, 1, 1),
                "ada@example.com", "+2348000000000", address, "12345678901", "CEO", null);
    }
}
