package com.atlashub.hr.attendance.application.query.GetAttendanceSummary;

import com.atlashub.hr.attendance.domain.valueobject.AttendanceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public record AttendanceDayResult(
        LocalDate date,
        ZonedDateTime clockIn,
        ZonedDateTime clockOut,
        BigDecimal hoursWorked,
        BigDecimal overtimeHours,
        AttendanceStatus status
) {}
