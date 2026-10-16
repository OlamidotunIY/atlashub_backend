package com.atlashub.hr.payroll.application.query.GetPayslip;

import com.atlashub.hr.payroll.domain.valueobject.PayslipStatus;
import com.atlashub.shared.domain.valueobject.Money;

import java.util.List;

public record PayslipResult(
        Long id,
        Long payrollRunId,
        Long employeeId,
        String employeeName,
        Money grossPay,
        Money totalAllowances,
        Money totalDeductions,
        Money netPay,
        List<PayslipLineItemResult> breakdown,
        PayslipStatus status
) {
}
