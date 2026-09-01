package com.atlashub.accounts.application.result;

public record AccountIssuanceRequestDto(
        String referenceId,
        String accountName,
        String bankName
) {
}
