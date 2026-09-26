package com.atlashub.compliance.application.commands.RejectCompliance;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RejectComplianceHandler extends Command<RejectComplianceCommand, ComplianceRecord> {

    private static final Logger log = LoggerFactory.getLogger(RejectComplianceHandler.class);

    private final ComplianceRecordRepository repository;

    public RejectComplianceHandler(ComplianceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public ComplianceRecord execute(RejectComplianceCommand input) {
        log.info("Executing RejectComplianceCommand for organizationId: {}", input.organizationId());

        ComplianceRecord record = repository.findByOrganizationId(input.organizationId())
            .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + input.organizationId()));

        record.reject(input.adminId(), input.reason());
        return repository.save(record);
    }
}
