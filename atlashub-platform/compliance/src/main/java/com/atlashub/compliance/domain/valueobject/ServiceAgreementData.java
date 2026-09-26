package com.atlashub.compliance.domain.valueobject;

import java.time.ZonedDateTime;
import com.atlashub.shared.domain.exception.ValidationException;

public record ServiceAgreementData(
    ZonedDateTime acceptedAt,
    String ipAddress,
    String termsVersion
) {
    public ServiceAgreementData {
        if (acceptedAt == null) {
            throw new ValidationException("acceptedAt cannot be null");
        }
        if (ipAddress == null || ipAddress.isBlank()) {
            throw new ValidationException("ipAddress cannot be blank");
        }
        if (termsVersion == null || termsVersion.isBlank()) {
            throw new ValidationException("termsVersion cannot be blank");
        }
    }
}
