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
        name = "analytics_payroll_cost",
        indexes = {
                @Index(name = "idx_payroll_cost_org_month", columnList = "organization_id, metric_month"),
                @Index(name = "idx_payroll_cost_unique", columnList = "organization_id, environment, metric_month", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PayrollCostProjectionJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "environment", nullable = false)
    private String environment;

    @Column(name = "metric_month", nullable = false, length = 7)
    private String metricMonth;

    @Column(name = "total_gross_payroll", precision = 19, scale = 4)
    private BigDecimal totalGrossPayroll;

    @Column(name = "total_deductions", precision = 19, scale = 4)
    private BigDecimal totalDeductions;

    @Column(name = "total_net_payroll", precision = 19, scale = 4)
    private BigDecimal totalNetPayroll;

    @Column(name = "headcount", nullable = false)
    private int headcount;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
