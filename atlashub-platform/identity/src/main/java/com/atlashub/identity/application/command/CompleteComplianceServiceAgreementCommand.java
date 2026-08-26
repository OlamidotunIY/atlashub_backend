package com.atlashub.identity.application.command;


public record CompleteComplianceServiceAgreementCommand(
    Long OrganizationId,
    boolean agreed
) {}
