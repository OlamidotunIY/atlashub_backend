package com.atlashub.hr.attendance.application.command.MarkAttendanceAsLeave;

import java.time.LocalDate;

public record MarkAttendanceAsLeaveCommand(
        Long employeeId,
        Long organizationId,
        LocalDate startDate,
        LocalDate endDate
) {}
