package com.atlashub.compliance.application.commands.UpdateBusinessProfile;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdateBusinessProfileHandler extends Command<UpdateBusinessProfileCommand, ComplianceRecord> {

    private static final Logger log = LoggerFactory.getLogger(UpdateBusinessProfileHandler.class);

    private final ComplianceRecordRepository repository;

    public UpdateBusinessProfileHandler(ComplianceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public ComplianceRecord execute(UpdateBusinessProfileCommand input) {
        log.info("Executing UpdateBusinessProfileCommand for organizationId: {}", input.organizationId());

        ComplianceRecord record = repository.findByOrganizationId(input.organizationId())
            .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + input.organizationId()));

        record.updateBusinessProfile(input.data());
        return repository.save(record);
    }
}
