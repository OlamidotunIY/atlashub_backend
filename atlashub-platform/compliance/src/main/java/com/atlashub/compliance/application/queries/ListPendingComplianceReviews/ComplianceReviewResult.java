package com.atlashub.compliance.application.queries.ListPendingComplianceReviews;

import com.atlashub.compliance.domain.valueobject.ComplianceStatus;
import java.time.ZonedDateTime;

public record ComplianceReviewResult(
    Long organizationId,
    ComplianceStatus status,
    ZonedDateTime submittedAt
) {
}
