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
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "analytics_financial_health",
        indexes = {
                @Index(name = "idx_financial_health_org_period", columnList = "organization_id, period_end"),
                @Index(name = "idx_financial_health_unique", columnList = "organization_id, environment, period_end", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FinancialHealthProjectionJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "environment", nullable = false)
    private String environment;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "revenue", precision = 19, scale = 4)
    private BigDecimal revenue;

    @Column(name = "cost_of_sales", precision = 19, scale = 4)
    private BigDecimal costOfSales;

    @Column(name = "gross_profit", precision = 19, scale = 4)
    private BigDecimal grossProfit;

    @Column(name = "gross_margin", precision = 7, scale = 4)
    private BigDecimal grossMargin;

    @Column(name = "operating_expense", precision = 19, scale = 4)
    private BigDecimal operatingExpense;

    @Column(name = "net_operating_income", precision = 19, scale = 4)
    private BigDecimal netOperatingIncome;

    @Column(name = "cash_balance", precision = 19, scale = 4)
    private BigDecimal cashBalance;

    @Column(name = "accounts_receivable", precision = 19, scale = 4)
    private BigDecimal accountsReceivable;

    @Column(name = "accounts_payable", precision = 19, scale = 4)
    private BigDecimal accountsPayable;

    @Column(name = "reconciliation_status", nullable = false)
    private String reconciliationStatus;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
