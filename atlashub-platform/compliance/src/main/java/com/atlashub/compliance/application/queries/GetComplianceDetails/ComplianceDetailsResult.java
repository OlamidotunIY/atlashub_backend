package com.atlashub.compliance.application.queries.GetComplianceDetails;

import com.atlashub.compliance.domain.valueobject.*;
import java.time.ZonedDateTime;
import java.util.Set;

public record ComplianceDetailsResult(
    Long id,
    Long organizationId,
    ComplianceStatus status,
    ComplianceStep currentStep,
    Set<ComplianceStep> completedSteps,
    Long reviewedBy,
    ZonedDateTime reviewedAt,
    String rejectionReason,
    ZonedDateTime submittedAt,
    ZonedDateTime approvedAt,
    BusinessProfileData businessProfile,
    ContactInfoData contactInfo,
    OwnerIdentityData ownerIdentity,
    SettlementAccountData settlementAccount,
    ServiceAgreementData serviceAgreement,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {
}
