package com.atlashub.identity.application.command;


public record CompleteComplianceServiceAgreementCommand(
    Long merchantId,
    boolean agreed
) {}
