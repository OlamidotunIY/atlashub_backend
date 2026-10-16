package com.atlashub.hr.leave.domain.entities;

import com.atlashub.hr.leave.domain.exceptions.InvalidLeaveStateException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

@Getter
public class LeaveType extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String name;
    private final Integer maxDaysPerYear;
    private final Boolean isPaid;
    private final Boolean requiresApproval;
    private final Boolean carryOverAllowed;
    private final Integer maxCarryOverDays;

    public LeaveType(
            Long id,
            Long organizationId,
            String name,
            Integer maxDaysPerYear,
            Boolean isPaid,
            Boolean requiresApproval,
            Boolean carryOverAllowed,
            Integer maxCarryOverDays
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.maxDaysPerYear = maxDaysPerYear;
        this.isPaid = isPaid;
        this.requiresApproval = requiresApproval;
        this.carryOverAllowed = carryOverAllowed;
        this.maxCarryOverDays = maxCarryOverDays;
    }

    public static LeaveType create(
            Long id,
            Long organizationId,
            String name,
            Integer maxDaysPerYear,
            Boolean isPaid,
            Boolean requiresApproval,
            Boolean carryOverAllowed,
            Integer maxCarryOverDays
    ) {
        if (organizationId == null) {
            throw new InvalidLeaveStateException("Organization ID must not be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidLeaveStateException("Leave type name must not be blank");
        }
        if (maxDaysPerYear == null || maxDaysPerYear <= 0) {
            throw new InvalidLeaveStateException("Max days per year must be greater than zero");
        }

        return new LeaveType(
                id,
                organizationId,
                name.trim(),
                maxDaysPerYear,
                isPaid != null ? isPaid : true,
                requiresApproval != null ? requiresApproval : true,
                carryOverAllowed != null ? carryOverAllowed : false,
                maxCarryOverDays != null ? maxCarryOverDays : 0
        );
    }

    @Override
    public Long getId() {
        return id;
    }
}
