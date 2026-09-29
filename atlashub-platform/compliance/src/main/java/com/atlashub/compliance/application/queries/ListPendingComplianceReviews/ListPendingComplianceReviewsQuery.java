package com.atlashub.compliance.application.queries.ListPendingComplianceReviews;

import com.atlashub.compliance.domain.valueobject.ComplianceStatus;

public record ListPendingComplianceReviewsQuery(
    ComplianceStatus status,
    int page,
    int size
) {
}
