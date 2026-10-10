package com.atlashub.pay.settlement.infrastructure.persistence.entities;

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
@Table(name = "pay_settlement_credit_evidence",
        uniqueConstraints = @UniqueConstraint(name = "uk_settlement_credit_reference",
                columnNames = {"api_environment", "anchor_transfer_reference"}),
        indexes = @Index(name = "idx_settlement_credit_match",
                columnList = "anchor_deposit_account_id,api_environment,amount,currency,matched_settlement_id"))
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
public class SettlementCreditEvidenceJpa implements BaseJpaEntity {
    @Id private Long id;
    @Column(name = "organization_id", nullable = false) private Long organizationId;
    @Enumerated(EnumType.STRING) @Column(name = "api_environment", nullable = false) private ApiEnvironment environment;
    @Column(name = "anchor_deposit_account_id", nullable = false) private Long anchorDepositAccountId;
    @Column(name = "anchor_transfer_reference", nullable = false) private String anchorTransferReference;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private CurrencyCode currency;
    @Column(name = "received_at", nullable = false) private ZonedDateTime receivedAt;
    @Column(name = "matched_settlement_id") private Long matchedSettlementId;
    @Column(name = "created_at", nullable = false) private ZonedDateTime createdAt;
    @Version private Long version;
}
