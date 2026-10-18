package com.atlashub.pay.controls.infrastructure.persistence.entities;

import com.atlashub.pay.controls.domain.valueobject.ControlMode;
import com.atlashub.pay.controls.domain.valueobject.Environment;
import com.atlashub.pay.controls.domain.valueobject.PolicyStatus;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "pay_controlled_funds_policies",
        indexes = {
                @Index(name = "idx_policy_facility_version", columnList = "facility_id, policy_version", unique = true),
                @Index(name = "idx_policy_org_status", columnList = "organization_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ControlledFundsPolicyJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false, length = 30)
    private Environment environment;

    @Column(name = "policy_version", nullable = false, length = 50)
    private String policyVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 50)
    private ControlMode mode;

    @Column(name = "permitted_categories", columnDefinition = "json")
    private String permittedCategories;

    @Column(name = "restricted_categories", columnDefinition = "json")
    private String restrictedCategories;

    @Column(name = "beneficiary_rules", columnDefinition = "json")
    private String beneficiaryRules;

    @Column(name = "withdrawal_rules", columnDefinition = "json")
    private String withdrawalRules;

    @Column(name = "amount_and_frequency_rules", columnDefinition = "json")
    private String amountAndFrequencyRules;

    @Column(name = "provider_capabilities", columnDefinition = "json")
    private String providerCapabilities;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PolicyStatus status;

    @Column(name = "effective_from", nullable = false)
    private ZonedDateTime effectiveFrom;

    @Column(name = "effective_until")
    private ZonedDateTime effectiveUntil;

    @Column(name = "activated_by", length = 100)
    private String activatedBy;

    @Version
    private Long version;
}
