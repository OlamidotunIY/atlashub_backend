package com.atlashub.compliance.application.commands.SubmitCompliance;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SubmitComplianceHandler extends Command<SubmitComplianceCommand, ComplianceRecord> {

    private static final Logger log = LoggerFactory.getLogger(SubmitComplianceHandler.class);

    private final ComplianceRecordRepository repository;

    public SubmitComplianceHandler(ComplianceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public ComplianceRecord execute(SubmitComplianceCommand input) {
        log.info("Executing SubmitComplianceCommand for organizationId: {}", input.organizationId());

        ComplianceRecord record = repository.findByOrganizationId(input.organizationId())
            .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + input.organizationId()));

        record.submit();
        return repository.save(record);
    }
}
