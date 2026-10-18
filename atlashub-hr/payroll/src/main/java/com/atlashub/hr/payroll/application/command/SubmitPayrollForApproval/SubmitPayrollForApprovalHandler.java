package com.atlashub.hr.payroll.application.command.SubmitPayrollForApproval;

import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.exceptions.PayrollRunNotFoundException;
import com.atlashub.hr.payroll.domain.repositories.PayrollRunRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SubmitPayrollForApprovalHandler extends Command<SubmitPayrollForApprovalCommand, Void> {

    private final PayrollRunRepository payrollRunRepository;

    public SubmitPayrollForApprovalHandler(PayrollRunRepository payrollRunRepository) {
        this.payrollRunRepository = payrollRunRepository;
    }

    @Override
    @Transactional
    public Void execute(SubmitPayrollForApprovalCommand command) {
        PayrollRun run = payrollRunRepository.findById(command.payrollRunId())
                .orElseThrow(() -> new PayrollRunNotFoundException(command.payrollRunId()));

        run.submitForApproval(command.initiatorUserId());
        payrollRunRepository.save(run);

        return null;
    }
}
