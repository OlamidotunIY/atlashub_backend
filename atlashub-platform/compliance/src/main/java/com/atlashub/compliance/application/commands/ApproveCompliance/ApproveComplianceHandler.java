package com.atlashub.compliance.application.commands.ApproveCompliance;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApproveComplianceHandler extends Command<ApproveComplianceCommand, ComplianceRecord> {

    private static final Logger log = LoggerFactory.getLogger(ApproveComplianceHandler.class);

    private final ComplianceRecordRepository repository;

    public ApproveComplianceHandler(ComplianceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public ComplianceRecord execute(ApproveComplianceCommand input) {
        log.info("Executing ApproveComplianceCommand for organizationId: {}", input.organizationId());

        ComplianceRecord record = repository.findByOrganizationId(input.organizationId())
            .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + input.organizationId()));

        record.approve(input.adminId());
        return repository.save(record);
    }
}
