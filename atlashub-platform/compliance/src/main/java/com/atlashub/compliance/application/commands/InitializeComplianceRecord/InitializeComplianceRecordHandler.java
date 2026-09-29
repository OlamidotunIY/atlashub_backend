package com.atlashub.compliance.application.commands.InitializeComplianceRecord;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InitializeComplianceRecordHandler extends Command<InitializeComplianceRecordCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(InitializeComplianceRecordHandler.class);

    private final ComplianceRecordRepository repository;

    public InitializeComplianceRecordHandler(ComplianceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void execute(InitializeComplianceRecordCommand input) {
        log.info("Executing InitializeComplianceRecordCommand for organizationId: {}", input.organizationId());

        Optional<ComplianceRecord> existing = repository.findByOrganizationId(input.organizationId());
        if (existing.isPresent()) {
            log.info("ComplianceRecord already exists for organizationId: {}. Skipping initialization.", input.organizationId());
            return null;
        }

        Long nextId = repository.nextIdentity();
        ComplianceRecord record = ComplianceRecord.create(nextId, input.organizationId());
        repository.save(record);

        return null;
    }
}
