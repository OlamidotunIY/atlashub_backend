package com.atlashub.accounts.application.dto;

public record AccountIssuanceRequestDto(
        String referenceId,
        String accountName,
        String bankName
) {
}
