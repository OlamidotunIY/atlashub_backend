package com.atlashub.compliance.application.commands.AcceptServiceAgreement;

public record AcceptServiceAgreementCommand(
    Long organizationId,
    String ipAddress,
    String termsVersion
) {
}
