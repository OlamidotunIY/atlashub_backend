package com.atlashub.hr.leave.application.command.ApplyForLeave;

import java.time.LocalDate;

public record ApplyForLeaveCommand(
        Long employeeId,
        Long organizationId,
        Long leaveTypeId,
        LocalDate startDate,
        LocalDate endDate,
        String reason
) {}
