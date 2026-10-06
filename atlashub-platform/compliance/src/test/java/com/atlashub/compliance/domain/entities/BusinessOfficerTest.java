package com.atlashub.compliance.domain.entities;

import com.atlashub.compliance.domain.exception.InvalidComplianceDataException;
import com.atlashub.compliance.domain.valueobject.*;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessOfficerTest {
    @Test
    void director_cannot_have_ownership_percentage() {
        assertThrows(InvalidComplianceDataException.class, () -> new BusinessOfficer(1L, OfficerRole.DIRECTOR,
                "Jane", null, "Doe", null, "NG", LocalDate.of(1990, 1, 1),
                new EmailAddress("jane@example.com"), new PhoneNumber("+2348012345678"),
                new AddressData("1 Main", null, "Ikeja", "Lagos", "100001", "NG"),
                "22222222226", "Director", BigDecimal.ONE, null, null));
    }
}
