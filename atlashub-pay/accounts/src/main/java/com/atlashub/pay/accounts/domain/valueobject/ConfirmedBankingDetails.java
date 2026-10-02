package com.atlashub.pay.accounts.domain.valueobject;

public record ConfirmedBankingDetails(
        String accountName,
        String accountNumber,
        String maskedAccountNumber,
        String bankName,
        String bankCode
) {
    public ConfirmedBankingDetails {
        if (accountName == null || accountName.isBlank() || accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Confirmed account name and number are required");
        }
    }
}
