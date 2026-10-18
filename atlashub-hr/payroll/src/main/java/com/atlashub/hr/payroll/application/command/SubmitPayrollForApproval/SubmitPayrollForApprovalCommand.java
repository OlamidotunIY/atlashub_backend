package com.atlashub.hr.payroll.application.command.SubmitPayrollForApproval;

public record SubmitPayrollForApprovalCommand(
        Long payrollRunId,
        Long initiatorUserId
) {
}
