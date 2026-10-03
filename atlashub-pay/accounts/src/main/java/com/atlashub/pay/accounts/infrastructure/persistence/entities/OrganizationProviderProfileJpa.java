package com.atlashub.pay.accounts.infrastructure.persistence.entities;

import com.atlashub.pay.accounts.domain.valueobject.PaymentCapability;
import com.atlashub.pay.accounts.domain.valueobject.PaymentProvider;
import com.atlashub.pay.accounts.domain.valueobject.ProviderProfileStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Set;

@Entity
@Table(name = "pay_organization_provider_profiles", uniqueConstraints = {@UniqueConstraint(name = "uk_pay_provider_profile_org_env_provider", columnNames = {"organization_id", "api_environment", "provider"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrganizationProviderProfileJpa implements BaseJpaEntity {
    @Id
    private Long id;
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    @Enumerated(EnumType.STRING)
    @Column(name = "api_environment", nullable = false)
    private ApiEnvironment environment;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentProvider provider;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderProfileStatus status;
    @Column(name = "external_merchant_id")
    private String externalMerchantId;
    @Column(name = "external_account_id")
    private String externalAccountId;
    @Column(name = "settlement_account_reference")
    private String settlementAccountReference;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pay_provider_requested_capabilities", joinColumns = @JoinColumn(name = "profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "capability", nullable = false)
    private Set<PaymentCapability> requestedCapabilities;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pay_provider_active_capabilities", joinColumns = @JoinColumn(name = "profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "capability", nullable = false)
    private Set<PaymentCapability> activeCapabilities;
    @Column(name = "onboarding_case_id")
    private Long onboardingCaseId;
    @Column(name = "failure_code")
    private String failureCode;
    @Column(name = "failure_message")
    private String failureMessage;
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
    @Version
    private Long version;
}
