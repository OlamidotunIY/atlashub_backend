package com.atlashub.compliance.application.commands.ReopenCompliance;

public record ReopenComplianceCommand(
    Long organizationId,
    Long adminId
) {
}
