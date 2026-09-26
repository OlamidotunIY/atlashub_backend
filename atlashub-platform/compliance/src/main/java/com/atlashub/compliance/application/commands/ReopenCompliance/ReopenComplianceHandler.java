package com.atlashub.compliance.application.commands.ReopenCompliance;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReopenComplianceHandler extends Command<ReopenComplianceCommand, ComplianceRecord> {

    private static final Logger log = LoggerFactory.getLogger(ReopenComplianceHandler.class);

    private final ComplianceRecordRepository repository;

    public ReopenComplianceHandler(ComplianceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public ComplianceRecord execute(ReopenComplianceCommand input) {
        log.info("Executing ReopenComplianceCommand for organizationId: {}", input.organizationId());

        ComplianceRecord record = repository.findByOrganizationId(input.organizationId())
            .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + input.organizationId()));

        record.reopen();
        return repository.save(record);
    }
}
