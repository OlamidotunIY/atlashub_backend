package com.atlashub.hr.payroll.application.command.InitiatePayroll;

import java.time.YearMonth;

public record InitiatePayrollCommand(
        Long organizationId,
        YearMonth period,
        Long initiatorUserId
) {
}
