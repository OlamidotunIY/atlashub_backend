package com.atlashub.compliance.domain.valueobject;

import java.time.LocalDate;
import com.atlashub.shared.domain.exception.ValidationException;
import com.atlashub.compliance.domain.exception.InvalidBvnFormatException;

public record OwnerIdentityData(
    String bvn,
    String nin,
    LocalDate dateOfBirth,
    GovernmentIdType govIdType,
    String govIdNumber,
    String govIdFrontUrl,
    String govIdBackUrl,
    String selfieUrl
) {
    public OwnerIdentityData {
        if (bvn == null || !bvn.matches("\\d{11}")) {
            throw new InvalidBvnFormatException("bvn must be exactly 11 digits");
        }
        if (dateOfBirth == null) {
            throw new ValidationException("dateOfBirth cannot be null");
        }
        if (govIdType == null) {
            throw new ValidationException("govIdType cannot be null");
        }
        if (govIdNumber == null || govIdNumber.isBlank()) {
            throw new ValidationException("govIdNumber cannot be blank");
        }
        if (govIdFrontUrl == null || govIdFrontUrl.isBlank()) {
            throw new ValidationException("govIdFrontUrl cannot be blank");
        }
        if (govIdBackUrl == null || govIdBackUrl.isBlank()) {
            throw new ValidationException("govIdBackUrl cannot be blank");
        }
        if (selfieUrl == null || selfieUrl.isBlank()) {
            throw new ValidationException("selfieUrl cannot be blank");
        }
    }
}
