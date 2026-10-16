package com.atlashub.hr.payroll.domain.entities;

import com.atlashub.hr.payroll.domain.valueobject.PayslipStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Payslip extends AggregateRoot<Long> {

    private final Long id;
    private final Long payrollRunId;
    private final Long employeeId;
    private final String employeeName;
    private final Money grossPay;
    private final Money totalAllowances;
    private final Money totalDeductions;
    private final Money netPay;
    private final List<PayslipLineItem> breakdown;
    private PayslipStatus status;

    public Payslip(
            Long id,
            Long payrollRunId,
            Long employeeId,
            String employeeName,
            Money grossPay,
            Money totalAllowances,
            Money totalDeductions,
            Money netPay,
            List<PayslipLineItem> breakdown,
            PayslipStatus status
    ) {
        this.id = id;
        this.payrollRunId = payrollRunId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.grossPay = grossPay;
        this.totalAllowances = totalAllowances;
        this.totalDeductions = totalDeductions;
        this.netPay = netPay;
        this.breakdown = breakdown != null ? new ArrayList<>(breakdown) : new ArrayList<>();
        this.status = status;
    }

    public static Payslip create(
            Long id,
            Long payrollRunId,
            Long employeeId,
            String employeeName,
            Money grossPay,
            Money totalAllowances,
            Money totalDeductions,
            Money netPay,
            List<PayslipLineItem> breakdown
    ) {
        return new Payslip(
                id,
                payrollRunId,
                employeeId,
                employeeName,
                grossPay,
                totalAllowances,
                totalDeductions,
                netPay,
                breakdown,
                PayslipStatus.DRAFT
        );
    }

    public void markDisbursed() {
        this.status = PayslipStatus.DISBURSED;
    }

    public void markFailed() {
        this.status = PayslipStatus.FAILED;
    }

    @Override
    public Long getId() {
        return id;
    }
}
