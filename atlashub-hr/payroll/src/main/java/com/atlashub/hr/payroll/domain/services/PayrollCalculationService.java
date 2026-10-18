package com.atlashub.hr.payroll.domain.services;

import com.atlashub.hr.payroll.domain.entities.Payslip;
import com.atlashub.hr.payroll.domain.entities.PayslipLineItem;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class PayrollCalculationService {

    public List<Payslip> calculatePayroll(Long organizationId, YearMonth period) {
        return new ArrayList<>();
    }

    public Payslip createPayslip(
            Long payrollRunId,
            Long employeeId,
            String employeeName,
            Money baseSalary,
            List<PayslipLineItem> allowances,
            List<PayslipLineItem> deductions
    ) {
        CurrencyCode currency = baseSalary != null ? baseSalary.currency() : CurrencyCode.NGN;
        Money totalAllowances = Money.zero(currency);
        Money totalDeductions = Money.zero(currency);

        List<PayslipLineItem> breakdown = new ArrayList<>();
        if (allowances != null) {
            for (PayslipLineItem a : allowances) {
                totalAllowances = totalAllowances.add(a.getAmount());
                breakdown.add(a);
            }
        }
        if (deductions != null) {
            for (PayslipLineItem d : deductions) {
                totalDeductions = totalDeductions.add(d.getAmount());
                breakdown.add(d);
            }
        }

        Money grossPay = baseSalary != null ? baseSalary.add(totalAllowances) : totalAllowances;
        Money netPay = grossPay.subtract(totalDeductions);

        return Payslip.create(
                null,
                payrollRunId,
                employeeId,
                employeeName,
                grossPay,
                totalAllowances,
                totalDeductions,
                netPay,
                breakdown
        );
    }
}
