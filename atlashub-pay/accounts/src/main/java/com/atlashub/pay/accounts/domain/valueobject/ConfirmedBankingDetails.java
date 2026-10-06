package com.atlashub.pay.accounts.domain.valueobject;

import com.atlashub.pay.accounts.domain.exceptions.InvalidBankingAccountDataException;

public record ConfirmedBankingDetails(
        String accountName,
        String accountNumber,
        String maskedAccountNumber,
        String bankName,
        String bankCode
) {
    public ConfirmedBankingDetails {
        boolean hasName = accountName != null && !accountName.isBlank();
        boolean hasNumber = accountNumber != null && !accountNumber.isBlank();
        if (hasName != hasNumber) {
            throw new InvalidBankingAccountDataException("Confirmed account name and number must be supplied together");
        }
    }

    public boolean hasAccountNumberDetails() {
        return accountName != null && !accountName.isBlank() && accountNumber != null && !accountNumber.isBlank();
    }
}
