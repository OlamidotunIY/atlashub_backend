package com.atlashub.hr.attendance.application.query.GetAttendanceSummary;

import java.math.BigDecimal;
import java.util.List;

public record AttendanceSummaryResult(
        int totalDays,
        int presentDays,
        int absentDays,
        int lateDays,
        BigDecimal totalHoursWorked,
        BigDecimal totalOvertimeHours,
        List<AttendanceDayResult> records
) {}
