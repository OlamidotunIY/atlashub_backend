package com.atlashub.compliance.infrastructure.persistence.entities;

import com.atlashub.compliance.domain.valueobject.BusinessProfileData;
import com.atlashub.compliance.domain.valueobject.ComplianceStatus;
import com.atlashub.compliance.domain.valueobject.ComplianceStep;
import com.atlashub.compliance.domain.valueobject.ContactInfoData;
import com.atlashub.compliance.domain.valueobject.OwnerIdentityData;
import com.atlashub.compliance.domain.valueobject.ServiceAgreementData;
import com.atlashub.compliance.domain.valueobject.AtlasHubEligibilityStatus;
import com.atlashub.compliance.domain.valueobject.AnchorVerificationStatus;
import com.atlashub.compliance.domain.valueobject.ComplianceDocumentsData;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Set;

@Entity
@Table(
    name = "compliance_records",
    indexes = {
        @Index(name = "idx_compliance_org_id", columnList = "organization_id", unique = true),
        @Index(name = "idx_compliance_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ComplianceRecordJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplianceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step")
    private ComplianceStep currentStep;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "completed_steps", columnDefinition = "json")
    private Set<ComplianceStep> completedSteps;

    @Enumerated(EnumType.STRING)
    @Column(name = "eligibility_status", nullable = false)
    private AtlasHubEligibilityStatus eligibilityStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "anchor_verification_status", nullable = false)
    private AnchorVerificationStatus anchorVerificationStatus;

    @Column(name = "anchor_business_customer_id", unique = true)
    private String anchorBusinessCustomerId;

    @Column(name = "failure_code")
    private String failureCode;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private ZonedDateTime reviewedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "submitted_at")
    private ZonedDateTime submittedAt;

    @Column(name = "approved_at")
    private ZonedDateTime approvedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "business_profile", columnDefinition = "json")
    private BusinessProfileData businessProfile;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contact_info", columnDefinition = "json")
    private ContactInfoData contactInfo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "owner_identity", columnDefinition = "json")
    private OwnerIdentityData ownerIdentity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "compliance_documents", columnDefinition = "json")
    private ComplianceDocumentsData complianceDocuments;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "service_agreement", columnDefinition = "json")
    private ServiceAgreementData serviceAgreement;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
