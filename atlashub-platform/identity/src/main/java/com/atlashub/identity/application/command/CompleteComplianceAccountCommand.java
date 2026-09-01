package com.atlashub.identity.application.command;


public record CompleteComplianceAccountCommand(
    Long OrganizationId,
    String settlementBankCode,
    String settlementAccountNumber
) {}
