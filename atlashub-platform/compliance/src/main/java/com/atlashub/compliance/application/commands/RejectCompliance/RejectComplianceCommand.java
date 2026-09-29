package com.atlashub.compliance.application.commands.RejectCompliance;

public record RejectComplianceCommand(
    Long organizationId,
    Long adminId,
    String reason
) {
}
