package com.atlashub.hr.payroll.application.command.ExpirePayrollApproval;

import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.exceptions.PayrollRunNotFoundException;
import com.atlashub.hr.payroll.domain.repositories.PayrollRunRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExpirePayrollApprovalHandler extends Command<ExpirePayrollApprovalCommand, Void> {

    private final PayrollRunRepository payrollRunRepository;

    public ExpirePayrollApprovalHandler(PayrollRunRepository payrollRunRepository) {
        this.payrollRunRepository = payrollRunRepository;
    }

    @Override
    @Transactional
    public Void execute(ExpirePayrollApprovalCommand command) {
        PayrollRun run = payrollRunRepository.findById(command.payrollRunId())
                .orElseThrow(() -> new PayrollRunNotFoundException(command.payrollRunId()));

        run.expire();
        payrollRunRepository.save(run);

        return null;
    }
}
