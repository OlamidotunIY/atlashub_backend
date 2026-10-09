package com.atlashub.compliance.infrastructure.persistence.entities;

import com.atlashub.compliance.domain.valueobject.DocumentStatus;
import com.atlashub.compliance.domain.valueobject.RequirementSource;
import com.atlashub.compliance.infrastructure.persistence.adapters.ComplianceSensitiveDataConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Entity @Table(name = "compliance_document_requirements", indexes = {
        @Index(name = "idx_compliance_document_record", columnList = "compliance_record_id"),
        @Index(name = "idx_compliance_document_anchor", columnList = "anchor_document_id", unique = true)})
@Data @Builder @NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
public class ComplianceDocumentRequirementJpa {
    @Id private Long id;
    @Column(name = "compliance_record_id", nullable = false) private Long complianceRecordId;
    @Column(name = "anchor_document_id", unique = true) private String anchorDocumentId;
    @Column(nullable = false) private String documentType;
    private String description;
    @Column(nullable = false) private boolean required;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RequirementSource source;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private DocumentStatus status;
    private String storageObjectKey;
    @Convert(converter = ComplianceSensitiveDataConverter.class) @Column(length = 4096) private String textValue;
    private String rejectionReason;
    @Column(nullable = false, updatable = false) private ZonedDateTime createdAt;
    @Column(nullable = false) private ZonedDateTime updatedAt;
}
