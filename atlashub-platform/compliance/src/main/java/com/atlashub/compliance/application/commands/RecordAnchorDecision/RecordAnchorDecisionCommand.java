package com.atlashub.compliance.application.commands.RecordAnchorDecision;

public record RecordAnchorDecisionCommand(Long organizationId, String anchorBusinessCustomerId,
                                          boolean approved, String reason) {}
