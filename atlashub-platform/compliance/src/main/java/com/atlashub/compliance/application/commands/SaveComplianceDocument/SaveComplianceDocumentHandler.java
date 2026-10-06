package com.atlashub.compliance.application.commands.SaveComplianceDocument;

import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import com.atlashub.compliance.application.port.AnchorCompliancePort;
import com.atlashub.shared.application.port.StoredObjectQueryPort;
import com.atlashub.compliance.domain.valueobject.AnchorVerificationStatus;

@Component
public class SaveComplianceDocumentHandler extends Command<SaveComplianceDocumentCommand, ComplianceRecord> {
    private final ComplianceRecordRepository repository;
    private final AnchorCompliancePort anchor;
    private final StoredObjectQueryPort storedObjects;
    public SaveComplianceDocumentHandler(ComplianceRecordRepository repository, AnchorCompliancePort anchor,
                                         StoredObjectQueryPort storedObjects) {
        this.repository = repository; this.anchor = anchor; this.storedObjects = storedObjects;
    }
    @Override @PreAuthorize("hasAuthority('compliance:manage')")
    public ComplianceRecord execute(SaveComplianceDocumentCommand command) {
        if (command.storageObjectKey() != null
                && !command.storageObjectKey().startsWith("compliance/" + command.organizationId() + "/"))
            throw new com.atlashub.compliance.domain.exception.InvalidComplianceDataException(
                    "Compliance document does not belong to the active organization");
        ComplianceRecord record = repository.findByOrganizationId(command.organizationId())
                .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found"));
        record.saveComplianceDocument(command.requirementId(), command.storageObjectKey(), command.textValue());
        ComplianceRecord saved = repository.save(record);
        var requirement = saved.getDocumentRequirement(command.requirementId());
        if (saved.getAnchorBusinessCustomerId() != null && requirement.getAnchorDocumentId() != null) {
            AnchorCompliancePort.StoredDocument file = null;
            if (requirement.getStorageObjectKey() != null) {
                var stored = storedObjects.readPrivateObject(requirement.getStorageObjectKey());
                file = new AnchorCompliancePort.StoredDocument(stored.objectKey(), stored.contentType(), stored.bytes());
            }
            anchor.uploadDocument(saved.getAnchorBusinessCustomerId(), requirement.getAnchorDocumentId(),
                    requirement.getTextValue(), file);
            saved.recordDocumentUnderReview(requirement.getId());
            saved = repository.save(saved);
        }
        if (saved.getAnchorVerificationStatus() == AnchorVerificationStatus.CUSTOMER_CREATED
                && saved.areRequiredDocumentsSatisfied()) {
            anchor.triggerBusinessVerification(saved.getAnchorBusinessCustomerId());
            saved.recordVerificationTriggered();
            saved = repository.save(saved);
        }
        return saved;
    }
}
