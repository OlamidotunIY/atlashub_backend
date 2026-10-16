package com.atlashub.hr.payroll.infrastructure.persistence.entities;

import com.atlashub.hr.payroll.domain.valueobject.PayslipStatus;
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

@Entity
@Table(
        name = "hr_payslips",
        indexes = {
                @Index(name = "idx_payslip_payroll_run", columnList = "payroll_run_id"),
                @Index(name = "idx_payslip_employee", columnList = "employee_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PayslipJpaEntity implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "payroll_run_id", nullable = false)
    private Long payrollRunId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "gross_pay", precision = 19, scale = 4)
    private BigDecimal grossPay;

    @Column(name = "total_allowances", precision = 19, scale = 4)
    private BigDecimal totalAllowances;

    @Column(name = "total_deductions", precision = 19, scale = 4)
    private BigDecimal totalDeductions;

    @Column(name = "net_pay", precision = 19, scale = 4)
    private BigDecimal netPay;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PayslipStatus status;

    @Version
    private Long version;

    @Override
    public Long getId() {
        return id;
    }
}
