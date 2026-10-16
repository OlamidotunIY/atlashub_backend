package com.atlashub.hr.payroll.application.command.ApprovePayroll;

public record ApprovePayrollCommand(
        Long payrollRunId,
        Long approverUserId
) {
}
