package com.atlashub.compliance.application.commands.OrchestrateAnchorCompliance;

import com.atlashub.compliance.application.port.AnchorCompliancePort;
import com.atlashub.compliance.domain.entities.*;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.valueobject.DocumentStatus;
import com.atlashub.compliance.domain.valueobject.RequirementSource;
import com.atlashub.compliance.domain.valueobject.AnchorVerificationStatus;
import com.atlashub.shared.application.port.StoredObjectQueryPort;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrchestrateAnchorComplianceHandler extends Command<OrchestrateAnchorComplianceCommand, Void> {
    private final ComplianceRecordRepository repository;
    private final AnchorCompliancePort anchor;
    private final StoredObjectQueryPort storedObjects;

    public OrchestrateAnchorComplianceHandler(ComplianceRecordRepository repository, AnchorCompliancePort anchor,
                                              StoredObjectQueryPort storedObjects) {
        this.repository = repository; this.anchor = anchor; this.storedObjects = storedObjects;
    }

    @Override
    public Void execute(OrchestrateAnchorComplianceCommand command) {
        ComplianceRecord record = repository.findByOrganizationId(command.organizationId())
                .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found"));
        if (record.getAnchorBusinessCustomerId() == null) {
            AnchorCompliancePort.BusinessCustomerResult created = anchor.createBusinessCustomer(request(record));
            record.recordAnchorCustomerCreated(created.customerId(), created.officerIds());
            record = repository.save(record);
        }

        List<AnchorCompliancePort.DocumentRequirement> providerRequirements =
                anchor.fetchCustomerDocumentRequirements(record.getAnchorBusinessCustomerId());
        ComplianceRecord current = record;
        current.recordRequiredDocuments(providerRequirements.stream().map(requirement ->
                new ComplianceDocumentRequirement(repository.nextIdentity(), requirement.documentId(),
                        requirement.documentType(), requirement.description(), requirement.required(),
                        RequirementSource.ANCHOR_CUSTOMER, null, null, null, null, null, null)).toList());
        current = repository.save(current);

        for (ComplianceDocumentRequirement document : current.getDocumentRequirements()) {
            if (document.getAnchorDocumentId() == null || document.getStatus() != DocumentStatus.UPLOADED) continue;
            AnchorCompliancePort.StoredDocument file = null;
            if (document.getStorageObjectKey() != null) {
                StoredObjectQueryPort.StoredObject stored = storedObjects.readPrivateObject(document.getStorageObjectKey());
                file = new AnchorCompliancePort.StoredDocument(stored.objectKey(), stored.contentType(), stored.bytes());
            }
            anchor.uploadDocument(current.getAnchorBusinessCustomerId(), document.getAnchorDocumentId(),
                    document.getTextValue(), file);
            current.recordDocumentUnderReview(document.getId());
            current = repository.save(current);
        }
        if ((current.getAnchorVerificationStatus() == AnchorVerificationStatus.CUSTOMER_CREATED
                || current.getAnchorVerificationStatus() == AnchorVerificationStatus.ERROR)
                && current.areRequiredDocumentsSatisfied()) {
            anchor.triggerBusinessVerification(current.getAnchorBusinessCustomerId());
            current.recordVerificationTriggered();
            repository.save(current);
        }
        return null;
    }

    private AnchorCompliancePort.BusinessCustomerRequest request(ComplianceRecord record) {
        List<AnchorCompliancePort.Officer> officers = record.getOfficers().stream().map(officer ->
                new AnchorCompliancePort.Officer(officer.getId(), officer.getRole(), officer.getFirstName(),
                        officer.getMiddleName(), officer.getLastName(), officer.getMaidenName(), officer.getNationality(),
                        officer.getDateOfBirth(), officer.getEmail().value(), officer.getPhoneNumber().value(),
                        officer.getResidentialAddress(), officer.getBvn(), officer.getTitle(), officer.getPercentageOwned())).toList();
        return new AnchorCompliancePort.BusinessCustomerRequest(record.getOrganizationId(), record.getBusinessProfile(),
                record.getContactInfo(), officers);
    }
}
