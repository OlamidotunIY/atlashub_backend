package com.atlashub.compliance.domain.valueobject;

import com.atlashub.shared.domain.exception.ValidationException;

public record SettlementAccountData(
    String bankCode,
    String accountNumber,
    String accountName
) {
    public SettlementAccountData {
        if (bankCode == null || bankCode.isBlank()) {
            throw new ValidationException("bankCode cannot be blank");
        }
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new ValidationException("accountNumber cannot be blank");
        }
        if (accountName == null || accountName.isBlank()) {
            throw new ValidationException("accountName cannot be blank");
        }
    }
}
