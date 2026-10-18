package com.atlashub.logistics.shipping.infrastructure.persistence.entities;

import com.atlashub.logistics.shipping.domain.valueobject.IncentiveStatus;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "logistics_merchant_incentives",
        indexes = {
                @Index(name = "idx_incentive_org_month", columnList = "organization_id, billing_year_month", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MerchantShippingIncentiveJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "billing_year_month", nullable = false, length = 16)
    private String billingYearMonth;

    @Column(name = "eligible_shipment_count", nullable = false)
    private int eligibleShipmentCount;

    @Column(name = "total_baseline_cost_amount", precision = 19, scale = 4)
    private BigDecimal totalBaselineCostAmount;

    @Column(name = "total_baseline_cost_currency", length = 3)
    private String totalBaselineCostCurrency;

    @Column(name = "total_actual_cost_amount", precision = 19, scale = 4)
    private BigDecimal totalActualCostAmount;

    @Column(name = "total_actual_cost_currency", length = 3)
    private String totalActualCostCurrency;

    @Column(name = "total_net_savings_amount", precision = 19, scale = 4)
    private BigDecimal totalNetSavingsAmount;

    @Column(name = "total_net_savings_currency", length = 3)
    private String totalNetSavingsCurrency;

    @Column(name = "incentive_percentage", precision = 5, scale = 4, nullable = false)
    private BigDecimal incentivePercentage;

    @Column(name = "rebate_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal rebateAmount;

    @Column(name = "rebate_currency", length = 3, nullable = false)
    private String rebateCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private IncentiveStatus status;

    @Column(name = "credited_at")
    private ZonedDateTime creditedAt;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Version
    private Long version;
}
