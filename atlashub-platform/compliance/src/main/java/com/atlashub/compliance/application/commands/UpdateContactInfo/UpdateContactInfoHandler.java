package com.atlashub.compliance.application.commands.UpdateContactInfo;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdateContactInfoHandler extends Command<UpdateContactInfoCommand, ComplianceRecord> {

    private static final Logger log = LoggerFactory.getLogger(UpdateContactInfoHandler.class);

    private final ComplianceRecordRepository repository;

    public UpdateContactInfoHandler(ComplianceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public ComplianceRecord execute(UpdateContactInfoCommand input) {
        log.info("Executing UpdateContactInfoCommand for organizationId: {}", input.organizationId());

        ComplianceRecord record = repository.findByOrganizationId(input.organizationId())
            .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + input.organizationId()));

        record.updateContactInfo(input.data());
        return repository.save(record);
    }
}
