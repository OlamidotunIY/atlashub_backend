package com.atlashub.pay.charges.infrastructure.persistence.entities;

import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.pay.charges.domain.valueobject.ChargeStatus;
import com.atlashub.pay.charges.domain.valueobject.PaymentProvider;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "pay_charges", uniqueConstraints = {@UniqueConstraint(name = "uk_charge_org_env_reference", columnNames = {"organization_id", "api_environment", "reference"}), @UniqueConstraint(name = "uk_charge_provider_env_reference", columnNames = {"provider", "api_environment", "provider_reference"})}, indexes = @Index(name = "idx_charge_org_status", columnList = "organization_id,api_environment,status"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChargeJpa implements BaseJpaEntity {
    @Id
    private Long id;
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    @Enumerated(EnumType.STRING)
    @Column(name = "api_environment", nullable = false)
    private ApiEnvironment environment;
    @Column(nullable = false)
    private String reference;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyCode currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeChannel channel;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentProvider provider;
    @Column(name = "provider_profile_id", nullable = false)
    private Long providerProfileId;
    @Column(name = "provider_reference")
    private String providerReference;
    @Column(name = "source_system", nullable = false)
    private String sourceSystem;
    @Column(name = "source_reference_id", nullable = false)
    private String sourceReferenceId;
    @Column(name = "customer_reference_id")
    private String customerReferenceId;
    @Column(name = "provider_fee", precision = 19, scale = 4)
    private BigDecimal providerFee;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeStatus status;
    @Column(name = "authorization_url", length = 1024)
    private String authorizationUrl;
    @Column(name = "access_code")
    private String accessCode;
    @Column(name = "failure_message")
    private String failureMessage;
    @Column(name = "provider_refund_reference")
    private String providerRefundReference;
    @Column(name = "refund_reason")
    private String refundReason;
    @Column(name = "refunded_at")
    private ZonedDateTime refundedAt;
    @Column(name = "dispute_reference")
    private String disputeReference;
    @Column(name = "dispute_status")
    private String disputeStatus;
    @Column(name = "dispute_reason")
    private String disputeReason;
    @Column(name = "successful_at")
    private ZonedDateTime successfulAt;
    @Column(name = "expires_at", nullable = false)
    private ZonedDateTime expiresAt;
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
    @Version
    private Long version;
}
