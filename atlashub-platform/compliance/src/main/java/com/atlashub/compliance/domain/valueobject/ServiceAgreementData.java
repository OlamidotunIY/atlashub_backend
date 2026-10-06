package com.atlashub.compliance.domain.valueobject;

import java.time.ZonedDateTime;
import com.atlashub.compliance.domain.exception.InvalidComplianceDataException;

public record ServiceAgreementData(
    ZonedDateTime acceptedAt,
    String ipAddress,
    String termsVersion
) {
    public ServiceAgreementData {
        if (acceptedAt == null) {
            throw new InvalidComplianceDataException("Accepted time is required");
        }
        if (ipAddress == null || ipAddress.isBlank()) {
            throw new InvalidComplianceDataException("IP address is required");
        }
        if (termsVersion == null || termsVersion.isBlank()) {
            throw new InvalidComplianceDataException("Terms version is required");
        }
    }
}
