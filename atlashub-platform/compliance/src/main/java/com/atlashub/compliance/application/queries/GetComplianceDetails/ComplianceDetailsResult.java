package com.atlashub.compliance.application.queries.GetComplianceDetails;

import com.atlashub.compliance.domain.valueobject.*;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public record ComplianceDetailsResult(Long id, Long organizationId, ComplianceStatus status,
        ComplianceStep currentStep, Map<ComplianceStep, StepStatus> stepProgress,
        AtlasHubEligibilityStatus eligibilityStatus, AnchorVerificationStatus anchorVerificationStatus,
        String failureCode, String rejectionReason, ZonedDateTime submittedAt, ZonedDateTime approvedAt,
        BusinessSummary business, ContactInfoData contact, List<OfficerSummary> officers,
        List<DocumentSummary> documentRequirements, ServiceAgreementData serviceAgreement,
        ZonedDateTime createdAt, ZonedDateTime updatedAt) {
    public record BusinessSummary(String legalName, LegalRegistrationType registrationType,
            java.time.LocalDate registrationDate, boolean registrationNumberSupplied, boolean businessBvnSupplied,
            SupportedBusinessIndustry industry, String businessDescription, String website) {}
    public record OfficerSummary(Long id, OfficerRole role, String firstName, String lastName, String title,
            OfficerVerificationStatus verificationStatus) {}
    public record DocumentSummary(Long id, String documentType, String description, boolean required,
            DocumentStatus status, String rejectionReason) {}
}
