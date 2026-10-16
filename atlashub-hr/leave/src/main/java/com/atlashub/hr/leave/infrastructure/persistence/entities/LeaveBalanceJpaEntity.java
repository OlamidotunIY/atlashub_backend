package com.atlashub.hr.leave.infrastructure.persistence.entities;

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
import lombok.Setter;

@Entity
@Table(
        name = "hr_leave_balances",
        indexes = {
                @Index(name = "idx_hr_leave_bal_emp_type_yr", columnList = "employee_id, leave_type_id, year", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LeaveBalanceJpaEntity implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "leave_type_id", nullable = false)
    private Long leaveTypeId;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "entitlement", nullable = false)
    private Integer entitlement;

    @Column(name = "used", nullable = false)
    private Integer used;

    @Column(name = "remaining", nullable = false)
    private Integer remaining;

    @Version
    private Long version;

    @Override
    public Long getId() {
        return id;
    }
}
