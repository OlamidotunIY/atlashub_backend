package com.atlashub.identity.application.command;


public record RegisterSplitRecipientCommand(
    Long OrganizationId,
    String bankCode,
    String accountNumber,
    String description
) {}
