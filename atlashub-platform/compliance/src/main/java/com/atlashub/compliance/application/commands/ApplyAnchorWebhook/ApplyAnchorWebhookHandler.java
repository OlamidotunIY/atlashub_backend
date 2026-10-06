package com.atlashub.compliance.application.commands.ApplyAnchorWebhook;

import com.atlashub.compliance.application.port.AnchorCompliancePort;
import com.atlashub.compliance.domain.entities.*;
import com.atlashub.compliance.domain.exception.*;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.valueobject.RequirementSource;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.application.port.StoredObjectQueryPort;
import com.atlashub.compliance.domain.valueobject.DocumentStatus;
import org.springframework.stereotype.Component;
import java.util.Locale;

@Component
public class ApplyAnchorWebhookHandler extends Command<ApplyAnchorWebhookCommand, Void> {
    private final ComplianceRecordRepository repository;
    private final AnchorCompliancePort anchor;
    private final StoredObjectQueryPort storedObjects;
    public ApplyAnchorWebhookHandler(ComplianceRecordRepository repository, AnchorCompliancePort anchor,
                                     StoredObjectQueryPort storedObjects) {
        this.repository = repository; this.anchor = anchor; this.storedObjects = storedObjects;
    }

    @Override public Void execute(ApplyAnchorWebhookCommand command) {
        String type = command.eventType().toLowerCase(Locale.ROOT);
        ComplianceRecord record = find(command);
        switch (type) {
            case "customer.identification.awaitingdocument" -> {
                applyDocumentRequirements(record, command.reason());
            }
            case "document.approved" -> record.recordDocumentApproved(command.documentId());
            case "document.rejected" -> record.recordDocumentRejected(command.documentId(), requiredReason(command.reason()));
            case "customer.identification.approved" -> record.recordAnchorApproved();
            case "customer.identification.rejected" -> record.recordAnchorRejected(requiredReason(command.reason()));
            case "customer.identification.error" -> record.recordAnchorError(
                    command.failureCode() == null ? "ANCHOR_VERIFICATION_ERROR" : command.failureCode(),
                    requiredReason(command.reason()));
            default -> throw new InvalidComplianceDataException("Unsupported Anchor compliance event");
        }
        repository.save(record);
        return null;
    }

    private void applyDocumentRequirements(ComplianceRecord record, String reason) {
        var requirements = anchor.fetchCustomerDocumentRequirements(record.getAnchorBusinessCustomerId());
        record.recordRequiredDocuments(requirements.stream().map(item -> new ComplianceDocumentRequirement(
                repository.nextIdentity(), item.documentId(), item.documentType(), item.description(), item.required(),
                RequirementSource.ANCHOR_CUSTOMER, null, null, null, null, null, null)).toList());
        for (ComplianceDocumentRequirement document : record.getDocumentRequirements()) {
            if (document.getAnchorDocumentId() == null || document.getStatus() != DocumentStatus.UPLOADED) continue;
            AnchorCompliancePort.StoredDocument file = null;
            if (document.getStorageObjectKey() != null) {
                var stored = storedObjects.readPrivateObject(document.getStorageObjectKey());
                file = new AnchorCompliancePort.StoredDocument(stored.objectKey(), stored.contentType(), stored.bytes());
            }
            anchor.uploadDocument(record.getAnchorBusinessCustomerId(), document.getAnchorDocumentId(),
                    document.getTextValue(), file);
            record.recordDocumentUnderReview(document.getId());
        }
        if (!record.areRequiredDocumentsSatisfied()) record.recordAwaitingDocuments(reason);
    }

    private ComplianceRecord find(ApplyAnchorWebhookCommand command) {
        if (command.customerId() != null) return repository.findByAnchorBusinessCustomerId(command.customerId())
                .orElseThrow(() -> new ComplianceRecordNotFoundException("Unknown Anchor business customer"));
        if (command.documentId() != null) return repository.findByAnchorDocumentId(command.documentId())
                .orElseThrow(() -> new ComplianceRecordNotFoundException("Unknown Anchor compliance document"));
        throw new InvalidComplianceDataException("Anchor compliance event has no customer or document identifier");
    }

    private String requiredReason(String reason) {
        return reason == null || reason.isBlank() ? "Anchor did not provide a reason" : reason;
    }
}
