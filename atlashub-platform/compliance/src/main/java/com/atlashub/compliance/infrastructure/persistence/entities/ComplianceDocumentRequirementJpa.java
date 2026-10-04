package com.atlashub.compliance.infrastructure.persistence.entities;

import com.atlashub.compliance.domain.valueobject.*;
import com.atlashub.compliance.infrastructure.persistence.adapters.ComplianceSensitiveDataConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

@Entity @Table(name = "compliance_document_requirements", indexes = {
        @Index(name = "idx_compliance_document_record", columnList = "compliance_record_id"),
        @Index(name = "idx_compliance_document_anchor", columnList = "anchor_document_id", unique = true)})
@Data @Builder @NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
public class ComplianceDocumentRequirementJpa {
    @Id private Long id;
    @Column(name = "compliance_record_id", nullable = false, insertable = false, updatable = false) private Long complianceRecordId;
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
