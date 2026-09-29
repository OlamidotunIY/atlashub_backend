package com.atlashub.compliance.application.queries.GetComplianceStatus;

import com.atlashub.compliance.domain.valueobject.ComplianceStatus;
import com.atlashub.compliance.domain.valueobject.ComplianceStep;
import java.util.Set;

public record ComplianceStatusResult(
    ComplianceStep currentStep,
    ComplianceStatus status,
    Set<ComplianceStep> completedSteps
) {
}
