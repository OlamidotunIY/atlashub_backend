package com.atlashub.hr.leave.application.query.ListLeaveApplications;

import com.atlashub.hr.leave.domain.valueobject.LeaveStatus;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record LeaveApplicationResult(
        Long id,
        Long employeeId,
        String employeeName,
        String leaveTypeName,
        LocalDate startDate,
        LocalDate endDate,
        Integer daysRequested,
        String reason,
        LeaveStatus status,
        Long approvedBy,
        ZonedDateTime approvedAt
) {}
