package com.atlashub.analytics.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "analytics_wallet_balance_summary",
        indexes = {
                @Index(name = "idx_wallet_summary_unique", columnList = "organization_id, environment", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class WalletBalanceSummaryProjectionJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "environment", nullable = false)
    private String environment;

    @Column(name = "operating_balance", precision = 19, scale = 4)
    private BigDecimal operatingBalance;

    @Column(name = "escrow_balance", precision = 19, scale = 4)
    private BigDecimal escrowBalance;

    @Column(name = "payroll_reserve_balance", precision = 19, scale = 4)
    private BigDecimal payrollReserveBalance;

    @Column(name = "tax_holding_balance", precision = 19, scale = 4)
    private BigDecimal taxHoldingBalance;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
