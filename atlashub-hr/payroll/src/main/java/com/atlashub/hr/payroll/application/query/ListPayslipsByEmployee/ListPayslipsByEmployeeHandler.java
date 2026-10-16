package com.atlashub.hr.payroll.application.query.ListPayslipsByEmployee;

import com.atlashub.hr.payroll.application.query.GetPayslip.GetPayslipHandler;
import com.atlashub.hr.payroll.application.query.GetPayslip.PayslipResult;
import com.atlashub.hr.payroll.domain.repositories.PayslipRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListPayslipsByEmployeeHandler extends Query<ListPayslipsByEmployeeQuery, List<PayslipResult>> {

    private final PayslipRepository payslipRepository;

    public ListPayslipsByEmployeeHandler(PayslipRepository payslipRepository) {
        this.payslipRepository = payslipRepository;
    }

    @Override
    public List<PayslipResult> execute(ListPayslipsByEmployeeQuery query) {
        return payslipRepository.findByEmployeeIdAndYear(query.employeeId(), query.year())
                .stream()
                .map(GetPayslipHandler::toResult)
                .toList();
    }
}
