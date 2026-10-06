package com.atlashub.shared.application.port;

public interface ComplianceQueryPort {
    ComplianceDecision getDecision(Long organizationId);

    boolean isApproved(Long organizationId);

    ComplianceStatus getStatus(Long organizationId);

    enum ComplianceStatus {
        NOT_STARTED,
        IN_PROGRESS,
        SUBMITTED,
        ACTION_REQUIRED,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        SUSPENDED
    }

    record ComplianceDecision(Long organizationId, ComplianceStatus status, boolean canProvisionBanking,
                              String reasonCode, String anchorBusinessCustomerId) {}
}
