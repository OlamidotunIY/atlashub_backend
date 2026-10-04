package com.atlashub.compliance.application.commands.SaveComplianceDocument;

import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class SaveComplianceDocumentHandler extends Command<SaveComplianceDocumentCommand, ComplianceRecord> {
    private final ComplianceRecordRepository repository;
    public SaveComplianceDocumentHandler(ComplianceRecordRepository repository) { this.repository = repository; }
    @Override @PreAuthorize("hasAuthority('compliance:manage')")
    public ComplianceRecord execute(SaveComplianceDocumentCommand command) {
        ComplianceRecord record = repository.findByOrganizationId(command.organizationId())
                .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found"));
        record.saveComplianceDocument(command.requirementId(), command.storageObjectKey(), command.textValue());
        return repository.save(record);
    }
}
