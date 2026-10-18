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
        name = "analytics_financing_behaviour",
        indexes = {
                @Index(name = "idx_financing_beh_org_time", columnList = "organization_id, occurred_at"),
                @Index(name = "idx_financing_beh_unique", columnList = "organization_id, environment, payment_context_key", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FinancingBehaviourProjectionJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "environment", nullable = false)
    private String environment;

    @Column(name = "payment_context_key", nullable = false)
    private String paymentContextKey;

    @Column(name = "total_inflow", precision = 19, scale = 4)
    private BigDecimal totalInflow;

    @Column(name = "total_outflow", precision = 19, scale = 4)
    private BigDecimal totalOutflow;

    @Column(name = "average_daily_balance", precision = 19, scale = 4)
    private BigDecimal averageDailyBalance;

    @Column(name = "repayment_success_rate", precision = 7, scale = 4)
    private BigDecimal repaymentSuccessRate;

    @Column(name = "anomaly_count", nullable = false)
    private int anomalyCount;

    @Column(name = "occurred_at")
    private ZonedDateTime occurredAt;

    @Version
    private Long version;
}
