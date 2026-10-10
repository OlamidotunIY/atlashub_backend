package com.atlashub.pay.settlement.infrastructure.persistence.entities;

import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;
import com.atlashub.pay.settlement.domain.valueobject.SettlementStatus;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pay_settlements", uniqueConstraints = {@UniqueConstraint(name = "uk_settlement_provider_ref", columnNames = {"provider", "api_environment", "provider_settlement_id"}), @UniqueConstraint(name = "uk_settlement_anchor_ref", columnNames = {"anchor_transfer_reference"})}, indexes = {@Index(name = "idx_settlement_org_status", columnList = "organization_id, api_environment, status"), @Index(name = "idx_settlement_anchor_match", columnList = "anchor_deposit_account_id, status, net_amount, currency")})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SettlementJpa implements BaseJpaEntity {

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

    @Column(name = "provider_settlement_id", nullable = false)
    private String providerSettlementId;

    @Column(name = "provider_subaccount_code", nullable = false)
    private String providerSubaccountCode;

    @Column(name = "gross_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal grossAmount;

    @Column(name = "net_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal netAmount;

    @Column(name = "provider_fee_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal providerFeeAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyCode currency;

    @Column(name = "anchor_deposit_account_id", nullable = false)
    private Long anchorDepositAccountId;

    @ElementCollection
    @CollectionTable(name = "pay_settlement_transactions", joinColumns = @JoinColumn(name = "settlement_id"))
    @Column(name = "transaction_reference", nullable = false)
    private List<String> transactionReferences = new ArrayList<>();

    @Column(name = "anchor_transfer_reference")
    private String anchorTransferReference;

    @Column(name = "settled_at", nullable = false)
    private ZonedDateTime settledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    @Column(length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
