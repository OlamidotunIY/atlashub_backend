package com.atlashub.pay.accounts.application.commands.ApplyOrganizationBankingRestriction;

public record ApplyOrganizationBankingRestrictionCommand(
        Long organizationId, boolean restricted, String restrictionType, String reason, String operationId
) {}
