package com.atlashub.hr.payroll.infrastructure.persistence.entities;

import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "hr_payroll_runs",
        indexes = {
                @Index(name = "idx_payroll_org_period", columnList = "organization_id, period"),
                @Index(name = "idx_payroll_org_status", columnList = "organization_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PayrollRunJpaEntity implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "period", nullable = false, length = 7)
    private String period;

    @Column(name = "total_gross", precision = 19, scale = 4)
    private BigDecimal totalGross;

    @Column(name = "total_deductions", precision = 19, scale = 4)
    private BigDecimal totalDeductions;

    @Column(name = "total_net", precision = 19, scale = 4)
    private BigDecimal totalNet;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PayrollStatus status;

    @Column(name = "initiated_by", nullable = false)
    private Long initiatedBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private ZonedDateTime approvedAt;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Version
    private Long version;

    @Override
    public Long getId() {
        return id;
    }
}
