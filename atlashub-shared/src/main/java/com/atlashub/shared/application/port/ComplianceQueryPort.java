package com.atlashub.shared.application.port;

public interface ComplianceQueryPort {
    boolean isApproved(Long organizationId);

    ComplianceStatus getStatus(Long organizationId);

    enum ComplianceStatus {
        NOT_STARTED,
        IN_PROGRESS,
        SUBMITTED,
        UNDER_REVIEW,
        APPROVED,
        REJECTED
    }
}
