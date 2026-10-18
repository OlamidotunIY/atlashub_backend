package com.atlashub.hr.payroll.application.query.GetPayslip;

import com.atlashub.hr.payroll.domain.entities.Payslip;
import com.atlashub.hr.payroll.domain.entities.PayslipLineItem;
import com.atlashub.hr.payroll.domain.repositories.PayslipRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetPayslipHandler extends Query<GetPayslipQuery, PayslipResult> {

    private final PayslipRepository payslipRepository;

    public GetPayslipHandler(PayslipRepository payslipRepository) {
        this.payslipRepository = payslipRepository;
    }

    @Override
    public PayslipResult execute(GetPayslipQuery query) {
        Payslip payslip = payslipRepository.findById(query.payslipId())
                .orElseThrow(() -> new NotFoundException("Payslip not found: " + query.payslipId()));

        return toResult(payslip);
    }

    public static PayslipResult toResult(Payslip payslip) {
        List<PayslipLineItemResult> breakdown = payslip.getBreakdown().stream()
                .map(GetPayslipHandler::toLineItemResult)
                .toList();

        return new PayslipResult(
                payslip.getId(),
                payslip.getPayrollRunId(),
                payslip.getEmployeeId(),
                payslip.getEmployeeName(),
                payslip.getGrossPay(),
                payslip.getTotalAllowances(),
                payslip.getTotalDeductions(),
                payslip.getNetPay(),
                breakdown,
                payslip.getStatus()
        );
    }

    private static PayslipLineItemResult toLineItemResult(PayslipLineItem item) {
        return new PayslipLineItemResult(
                item.getName(),
                item.getType(),
                item.getAmount()
        );
    }
}
