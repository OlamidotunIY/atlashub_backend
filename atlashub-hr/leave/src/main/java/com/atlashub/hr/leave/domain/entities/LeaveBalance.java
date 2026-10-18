package com.atlashub.hr.leave.domain.entities;

import com.atlashub.hr.leave.domain.exceptions.InsufficientLeaveBalanceException;
import com.atlashub.hr.leave.domain.exceptions.InvalidLeaveStateException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

@Getter
public class LeaveBalance extends AggregateRoot<Long> {

    private final Long id;
    private final Long employeeId;
    private final Long leaveTypeId;
    private final Integer year;
    private Integer entitlement;
    private Integer used;
    private Integer remaining;

    public LeaveBalance(
            Long id,
            Long employeeId,
            Long leaveTypeId,
            Integer year,
            Integer entitlement,
            Integer used,
            Integer remaining
    ) {
        this.id = id;
        this.employeeId = employeeId;
        this.leaveTypeId = leaveTypeId;
        this.year = year;
        this.entitlement = entitlement;
        this.used = used;
        this.remaining = remaining;
    }

    public static LeaveBalance create(
            Long id,
            Long employeeId,
            Long leaveTypeId,
            Integer year,
            Integer entitlement
    ) {
        if (employeeId == null) {
            throw new InvalidLeaveStateException("Employee ID must not be null");
        }
        if (leaveTypeId == null) {
            throw new InvalidLeaveStateException("Leave type ID must not be null");
        }
        if (year == null || year <= 0) {
            throw new InvalidLeaveStateException("Year must be valid");
        }
        if (entitlement == null || entitlement < 0) {
            throw new InvalidLeaveStateException("Entitlement must not be negative");
        }

        return new LeaveBalance(
                id,
                employeeId,
                leaveTypeId,
                year,
                entitlement,
                0,
                entitlement
        );
    }

    public void deduct(int days) {
        if (days <= 0) {
            throw new InvalidLeaveStateException("Days to deduct must be greater than zero");
        }
        if (this.remaining < days) {
            throw new InsufficientLeaveBalanceException();
        }
        this.used += days;
        this.remaining -= days;
    }

    public void restore(int days) {
        if (days <= 0) {
            throw new InvalidLeaveStateException("Days to restore must be greater than zero");
        }
        this.used = Math.max(0, this.used - days);
        this.remaining = Math.min(this.entitlement, this.remaining + days);
    }

    @Override
    public Long getId() {
        return id;
    }
}
