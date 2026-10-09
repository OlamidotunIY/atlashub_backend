package com.atlashub.compliance.infrastructure.persistence.entities;

import com.atlashub.compliance.domain.valueobject.AnchorVerificationStatus;
import com.atlashub.compliance.domain.valueobject.AtlasHubEligibilityStatus;
import com.atlashub.compliance.domain.valueobject.ComplianceStatus;
import com.atlashub.compliance.domain.valueobject.ComplianceStep;
import com.atlashub.compliance.domain.valueobject.ContactInfoData;
import com.atlashub.compliance.domain.valueobject.LegalRegistrationType;
import com.atlashub.compliance.domain.valueobject.ServiceAgreementData;
import com.atlashub.compliance.domain.valueobject.StepStatus;
import com.atlashub.compliance.domain.valueobject.SupportedBusinessIndustry;
import com.atlashub.compliance.infrastructure.persistence.adapters.ComplianceSensitiveDataConverter;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;

@Entity
@Table(name = "compliance_records", indexes = {@Index(name = "idx_compliance_org_id", columnList = "organization_id", unique = true), @Index(name = "idx_compliance_status", columnList = "status")})
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ComplianceRecordJpa implements BaseJpaEntity {
    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplianceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false)
    private ComplianceStep currentStep;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "step_progress", nullable = false, columnDefinition = "json")
    private Map<ComplianceStep, StepStatus> stepProgress;
    @Enumerated(EnumType.STRING)
    @Column(name = "eligibility_status", nullable = false)
    private AtlasHubEligibilityStatus eligibilityStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "anchor_verification_status", nullable = false)
    private AnchorVerificationStatus anchorVerificationStatus;
    @Column(name = "anchor_business_customer_id", unique = true)
    private String anchorBusinessCustomerId;
    private String failureCode;
    private String rejectionReason;
    private ZonedDateTime submittedAt;
    private ZonedDateTime approvedAt;
    private String legalName;
    @Enumerated(EnumType.STRING)
    private LegalRegistrationType registrationType;
    private LocalDate registrationDate;
    @Convert(converter = ComplianceSensitiveDataConverter.class)
    @Column(length = 1024)
    private String businessRegistrationNumber;
    @Convert(converter = ComplianceSensitiveDataConverter.class)
    @Column(length = 1024)
    private String businessBvn;
    @Enumerated(EnumType.STRING)
    private SupportedBusinessIndustry industry;
    @Column(length = 2048)
    private String businessDescription;
    private String website;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private ContactInfoData contactInfo;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private ServiceAgreementData serviceAgreement;
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    @Column(nullable = false)
    private ZonedDateTime updatedAt;
    @Version
    private Long version;
}
