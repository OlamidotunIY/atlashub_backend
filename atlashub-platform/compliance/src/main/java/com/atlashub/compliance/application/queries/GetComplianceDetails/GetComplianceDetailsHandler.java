package com.atlashub.compliance.application.queries.GetComplianceDetails;

import com.atlashub.shared.application.usecase.Query;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GetComplianceDetailsHandler extends Query<GetComplianceDetailsQuery, ComplianceDetailsResult> {

    private static final Logger log = LoggerFactory.getLogger(GetComplianceDetailsHandler.class);

    private final ComplianceRecordRepository repository;

    public GetComplianceDetailsHandler(ComplianceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public ComplianceDetailsResult execute(GetComplianceDetailsQuery query) {
        log.info("Executing GetComplianceDetailsQuery for organizationId: {}", query.organizationId());

        ComplianceRecord record = repository.findByOrganizationId(query.organizationId())
            .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + query.organizationId()));

        return new ComplianceDetailsResult(
            record.getId(),
            record.getOrganizationId(),
            record.getStatus(),
            record.getCurrentStep(),
            record.getCompletedSteps(),
            record.getReviewedBy(),
            record.getReviewedAt(),
            record.getRejectionReason(),
            record.getSubmittedAt(),
            record.getApprovedAt(),
            record.getBusinessProfile(),
            record.getContactInfo(),
            record.getOwnerIdentity(),
            record.getSettlementAccount(),
            record.getServiceAgreement(),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }
}
