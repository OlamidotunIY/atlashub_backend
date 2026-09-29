package com.atlashub.compliance.application.commands.ApproveCompliance;

public record ApproveComplianceCommand(
    Long organizationId,
    Long adminId
) {
}
