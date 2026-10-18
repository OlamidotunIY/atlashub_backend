package com.atlashub.hr.payroll.application.query.ListPayrollRuns;

import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.YearMonth;
import java.time.ZonedDateTime;

public record PayrollRunResult(
        Long id,
        YearMonth period,
        Money totalGross,
        Money totalDeductions,
        Money totalNet,
        PayrollStatus status,
        Long initiatedBy,
        Long approvedBy,
        ZonedDateTime approvedAt,
        ZonedDateTime createdAt
) {
}
