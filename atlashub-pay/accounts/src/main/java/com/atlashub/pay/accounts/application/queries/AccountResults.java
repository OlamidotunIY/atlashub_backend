package com.atlashub.pay.accounts.application.queries;

import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.valueobject.ReservedAccountOwnerType;

import java.time.ZonedDateTime;

public final class AccountResults {
    private AccountResults() {
    }

    public record ExternalAccountResult(
            Long id, String accountName, String maskedAccountNumber, String bankName,
            String bankCode, String currency, ExternalAccountStatus status, ZonedDateTime activatedAt) {
    }

    public record BusinessBankingResult(
            Long profileId, String status, ExternalAccountResult depositAccount,
            ExternalAccountResult subAccount) {
    }

    public record ReservedAccountResult(
            Long id, ReservedAccountOwnerType ownerType, String ownerReferenceId, String provider,
            String accountName, String maskedAccountNumber, String bankName,
            String bankCode, String currency, ExternalAccountStatus status, ZonedDateTime activatedAt) {
    }
}
