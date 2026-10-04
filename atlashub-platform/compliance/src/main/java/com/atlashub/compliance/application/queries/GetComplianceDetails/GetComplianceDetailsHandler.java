package com.atlashub.compliance.application.queries.GetComplianceDetails;

import com.atlashub.shared.application.usecase.Query;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.security.access.prepost.PreAuthorize;

@Component
public class GetComplianceDetailsHandler extends Query<GetComplianceDetailsQuery, ComplianceDetailsResult> {

    private static final Logger log = LoggerFactory.getLogger(GetComplianceDetailsHandler.class);

    private final ComplianceRecordRepository repository;

    public GetComplianceDetailsHandler(ComplianceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    @PreAuthorize("hasAuthority('compliance:read')")
    public ComplianceDetailsResult execute(GetComplianceDetailsQuery query) {
        log.info("Executing GetComplianceDetailsQuery for organizationId: {}", query.organizationId());

        ComplianceRecord record = repository.findByOrganizationId(query.organizationId())
            .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + query.organizationId()));

        return new ComplianceDetailsResult(
            record.getId(),
            record.getOrganizationId(),
            record.getStatus(),
            record.getCurrentStep(),
            record.getStepProgress(),
            record.getEligibilityStatus(),
            record.getAnchorVerificationStatus(),
            record.getFailureCode(),
            record.getRejectionReason(),
            record.getSubmittedAt(),
            record.getApprovedAt(),
            new ComplianceDetailsResult.BusinessSummary(record.getBusinessProfile().legalName(),
                    record.getBusinessProfile().registrationType(), record.getBusinessProfile().registrationDate(),
                    record.getBusinessProfile().businessRegistrationNumber() != null,
                    record.getBusinessProfile().businessBvn() != null, record.getBusinessProfile().industry(),
                    record.getBusinessProfile().businessDescription(), record.getBusinessProfile().website()),
            record.getContactInfo(),
            record.getOfficers().stream().map(officer -> new ComplianceDetailsResult.OfficerSummary(
                    officer.getId(), officer.getRole(), officer.getFirstName(), officer.getLastName(),
                    officer.getTitle(), officer.getVerificationStatus())).toList(),
            record.getDocumentRequirements().stream().map(document -> new ComplianceDetailsResult.DocumentSummary(
                    document.getId(), document.getDocumentType(), document.getDescription(), document.isRequired(),
                    document.getStatus(), document.getRejectionReason())).toList(),
            record.getServiceAgreement(),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }
}
