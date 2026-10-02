package com.atlashub.compliance.application.commands.UpdateComplianceDocuments;

import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class UpdateComplianceDocumentsHandler
        extends Command<UpdateComplianceDocumentsCommand, ComplianceRecord> {
    private final ComplianceRecordRepository repository;

    public UpdateComplianceDocumentsHandler(ComplianceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public ComplianceRecord execute(UpdateComplianceDocumentsCommand input) {
        ComplianceRecord record = repository.findByOrganizationId(input.organizationId())
                .orElseThrow(() -> new ComplianceRecordNotFoundException(
                        "Compliance record not found for org: " + input.organizationId()));
        record.updateComplianceDocuments(input.data());
        return repository.save(record);
    }
}
