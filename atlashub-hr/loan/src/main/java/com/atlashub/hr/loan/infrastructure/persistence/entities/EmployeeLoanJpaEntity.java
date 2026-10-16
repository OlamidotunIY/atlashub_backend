package com.atlashub.hr.loan.infrastructure.persistence.entities;

import com.atlashub.hr.loan.domain.valueobject.LoanStatus;
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
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "hr_employee_loans",
        indexes = {
                @Index(name = "idx_loan_emp_status", columnList = "employee_id, status"),
                @Index(name = "idx_loan_org_status", columnList = "organization_id, status"),
                @Index(name = "idx_loan_org_emp", columnList = "organization_id, employee_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EmployeeLoanJpaEntity implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "principal_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal principalAmount;

    @Column(name = "outstanding_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal outstandingBalance;

    @Column(name = "monthly_deduction", nullable = false, precision = 19, scale = 4)
    private BigDecimal monthlyDeduction;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "disbursed_at")
    private ZonedDateTime disbursedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanStatus status;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Version
    private Long version;

    @Override
    public Long getId() {
        return id;
    }
}
