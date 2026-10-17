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
        name = "analytics_transaction_volume",
        indexes = {
                @Index(name = "idx_tx_volume_org_month", columnList = "organization_id, metric_month"),
                @Index(name = "idx_tx_volume_unique", columnList = "organization_id, environment, metric_month", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TransactionVolumeProjectionJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "environment", nullable = false)
    private String environment;

    @Column(name = "metric_month", nullable = false, length = 7)
    private String metricMonth;

    @Column(name = "total_charges", nullable = false)
    private long totalCharges;

    @Column(name = "total_volume", precision = 19, scale = 4)
    private BigDecimal totalVolume;

    @Column(name = "successful_charges", nullable = false)
    private long successfulCharges;

    @Column(name = "failed_charges", nullable = false)
    private long failedCharges;

    @Column(name = "total_payouts", nullable = false)
    private long totalPayouts;

    @Column(name = "total_payout_amount", precision = 19, scale = 4)
    private BigDecimal totalPayoutAmount;

    @Column(name = "platform_fee_collected", precision = 19, scale = 4)
    private BigDecimal platformFeeCollected;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
