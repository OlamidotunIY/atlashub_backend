package com.atlashub.hr.attendance.application.query.GetAttendanceSummary;

public record GetAttendanceSummaryQuery(
        Long organizationId,
        Long employeeId,
        int month,
        int year
) {}
