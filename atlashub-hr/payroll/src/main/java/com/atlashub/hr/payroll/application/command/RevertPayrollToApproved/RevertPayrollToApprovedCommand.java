package com.atlashub.hr.payroll.application.command.RevertPayrollToApproved;

public record RevertPayrollToApprovedCommand(
        Long payrollRunId,
        String failureReason
) {
}
