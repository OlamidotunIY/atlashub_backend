package com.atlashub.compliance.application.queries.GetComplianceStatus;

import com.atlashub.compliance.domain.valueobject.ComplianceStatus;
import com.atlashub.compliance.domain.valueobject.ComplianceStep;
import java.util.Set;
import com.atlashub.compliance.domain.valueobject.AnchorVerificationStatus;
import com.atlashub.compliance.domain.valueobject.AtlasHubEligibilityStatus;

public record ComplianceStatusResult(
    ComplianceStep currentStep,
    ComplianceStatus status,
    Set<ComplianceStep> completedSteps,
    AtlasHubEligibilityStatus eligibilityStatus,
    AnchorVerificationStatus anchorVerificationStatus,
    String rejectionReason
) {
}
