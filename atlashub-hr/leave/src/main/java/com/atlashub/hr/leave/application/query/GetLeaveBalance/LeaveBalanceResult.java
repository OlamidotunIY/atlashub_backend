package com.atlashub.hr.leave.application.query.GetLeaveBalance;

public record LeaveBalanceResult(
        Long leaveTypeId,
        String leaveTypeName,
        int year,
        int entitlement,
        int used,
        int remaining
) {}
