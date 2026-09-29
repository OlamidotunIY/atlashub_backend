package com.atlashub.compliance.application.commands.UpdateOwnerIdentity;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdateOwnerIdentityHandler extends Command<UpdateOwnerIdentityCommand, ComplianceRecord> {

    private static final Logger log = LoggerFactory.getLogger(UpdateOwnerIdentityHandler.class);

    private final ComplianceRecordRepository repository;

    public UpdateOwnerIdentityHandler(ComplianceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public ComplianceRecord execute(UpdateOwnerIdentityCommand input) {
        log.info("Executing UpdateOwnerIdentityCommand for organizationId: {}", input.organizationId());

        ComplianceRecord record = repository.findByOrganizationId(input.organizationId())
            .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + input.organizationId()));

        record.updateOwnerIdentity(input.data());
        return repository.save(record);
    }
}
