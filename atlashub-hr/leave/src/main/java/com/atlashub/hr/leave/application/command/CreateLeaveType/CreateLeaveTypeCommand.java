package com.atlashub.hr.leave.application.command.CreateLeaveType;

public record CreateLeaveTypeCommand(
        Long organizationId,
        String name,
        int maxDaysPerYear,
        boolean isPaid,
        boolean requiresApproval,
        boolean carryOverAllowed,
        int maxCarryOverDays
) {}
