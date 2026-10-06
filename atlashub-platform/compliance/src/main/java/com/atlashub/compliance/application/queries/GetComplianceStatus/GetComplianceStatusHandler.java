package com.atlashub.compliance.application.queries.GetComplianceStatus;

import com.atlashub.shared.application.usecase.Query;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.security.access.prepost.PreAuthorize;

@Component
public class GetComplianceStatusHandler extends Query<GetComplianceStatusQuery, ComplianceStatusResult> {

    private static final Logger log = LoggerFactory.getLogger(GetComplianceStatusHandler.class);

    private final ComplianceRecordRepository repository;

    public GetComplianceStatusHandler(ComplianceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    @PreAuthorize("hasAuthority('compliance:read')")
    public ComplianceStatusResult execute(GetComplianceStatusQuery query) {
        log.info("Executing GetComplianceStatusQuery for organizationId: {}", query.organizationId());

        ComplianceRecord record = repository.findByOrganizationId(query.organizationId())
            .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + query.organizationId()));

        return new ComplianceStatusResult(
            record.getCurrentStep(),
            record.getStatus(),
            record.getStepProgress(),
            record.getEligibilityStatus(),
            record.getAnchorVerificationStatus(),
            record.getRejectionReason()
        );
    }
}
