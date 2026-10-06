package com.atlashub.compliance.infrastructure.persistence.entities;

import com.atlashub.compliance.domain.valueobject.*;
import com.atlashub.compliance.infrastructure.persistence.adapters.ComplianceSensitiveDataConverter;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
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
    @Column(name = "sandbox_anchor_business_customer_id", unique = true)
    private String sandboxAnchorBusinessCustomerId;
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
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "compliance_record_id")
    @Builder.Default
    private List<BusinessOfficerJpa> officers = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "compliance_record_id")
    @Builder.Default
    private List<ComplianceDocumentRequirementJpa> documentRequirements = new ArrayList<>();
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    @Column(nullable = false)
    private ZonedDateTime updatedAt;
    @Version
    private Long version;
}
