package com.atlashub.compliance.domain.entities;

import com.atlashub.compliance.domain.exception.InvalidComplianceDataException;
import com.atlashub.compliance.domain.valueobject.DocumentStatus;
import com.atlashub.compliance.domain.valueobject.RequirementSource;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public final class ComplianceDocumentRequirement {
    private final Long id;
    private String anchorDocumentId;
    private final String documentType;
    private String description;
    private final boolean required;
    private RequirementSource source;
    private DocumentStatus status;
    private String storageObjectKey;
    private String textValue;
    private String rejectionReason;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public ComplianceDocumentRequirement(Long id, String anchorDocumentId, String documentType, String description,
                                         boolean required, RequirementSource source, DocumentStatus status,
                                         String storageObjectKey, String textValue, String rejectionReason,
                                         ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        if (id == null) throw new InvalidComplianceDataException("Document requirement id is required");
        if (documentType == null || documentType.isBlank())
            throw new InvalidComplianceDataException("Document type is required");
        this.id = id; this.anchorDocumentId = anchorDocumentId; this.documentType = documentType;
        this.description = description; this.required = required;
        this.source = source == null ? RequirementSource.PREFLIGHT : source;
        this.status = status == null ? DocumentStatus.REQUESTED : status;
        this.storageObjectKey = storageObjectKey; this.textValue = textValue;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt == null ? ZonedDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    public void save(String objectKey, String value) {
        if ((objectKey == null || objectKey.isBlank()) && (value == null || value.isBlank()))
            throw new InvalidComplianceDataException("A document file or textual value is required");
        storageObjectKey = objectKey; textValue = value; status = DocumentStatus.UPLOADED;
        rejectionReason = null; updatedAt = ZonedDateTime.now();
    }

    public void identifyByAnchor(String anchorId, String providerDescription) {
        if (anchorId == null || anchorId.isBlank()) throw new InvalidComplianceDataException("Anchor document id is required");
        anchorDocumentId = anchorId; description = providerDescription; source = RequirementSource.ANCHOR_CUSTOMER;
        updatedAt = ZonedDateTime.now();
    }

    public void markUnderReview() { status = DocumentStatus.UNDER_REVIEW; updatedAt = ZonedDateTime.now(); }
    public void approve() { status = DocumentStatus.APPROVED; rejectionReason = null; updatedAt = ZonedDateTime.now(); }
    public void reject(String reason) {
        if (reason == null || reason.isBlank()) throw new InvalidComplianceDataException("Document rejection reason is required");
        status = DocumentStatus.REJECTED; rejectionReason = reason; updatedAt = ZonedDateTime.now();
    }

    public boolean isSatisfied() { return !required || status == DocumentStatus.UPLOADED
            || status == DocumentStatus.UNDER_REVIEW || status == DocumentStatus.APPROVED; }
}
