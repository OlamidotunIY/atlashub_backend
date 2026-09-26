package com.atlashub.compliance.domain.valueobject;

import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.exception.ValidationException;

public record BusinessProfileData(
    String businessDescription,
    String industry,
    String annualTransactionVolume,
    Money expectedMonthlyVolume,
    Integer staffCount
) {
    public BusinessProfileData {
        if (businessDescription == null || businessDescription.isBlank()) {
            throw new ValidationException("businessDescription cannot be blank");
        }
        if (industry == null || industry.isBlank()) {
            throw new ValidationException("industry cannot be blank");
        }
        if (annualTransactionVolume == null || annualTransactionVolume.isBlank()) {
            throw new ValidationException("annualTransactionVolume cannot be blank");
        }
        if (expectedMonthlyVolume == null) {
            throw new ValidationException("expectedMonthlyVolume cannot be null");
        }
        if (staffCount == null || staffCount < 1) {
            throw new ValidationException("staffCount must be at least 1");
        }
    }
}
