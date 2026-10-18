package com.atlashub.hr.payroll.application.query.ListPayrollRuns;

import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;

public record ListPayrollRunsQuery(
        Long organizationId,
        PayrollStatus status
) {
}
