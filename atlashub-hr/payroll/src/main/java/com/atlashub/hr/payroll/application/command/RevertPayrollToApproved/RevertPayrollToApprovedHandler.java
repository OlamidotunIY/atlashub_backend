package com.atlashub.hr.payroll.application.command.RevertPayrollToApproved;

import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.exceptions.PayrollRunNotFoundException;
import com.atlashub.hr.payroll.domain.repositories.PayrollRunRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RevertPayrollToApprovedHandler extends Command<RevertPayrollToApprovedCommand, Void> {

    private final PayrollRunRepository payrollRunRepository;

    public RevertPayrollToApprovedHandler(PayrollRunRepository payrollRunRepository) {
        this.payrollRunRepository = payrollRunRepository;
    }

    @Override
    @Transactional
    public Void execute(RevertPayrollToApprovedCommand command) {
        PayrollRun run = payrollRunRepository.findById(command.payrollRunId())
                .orElseThrow(() -> new PayrollRunNotFoundException(command.payrollRunId()));

        run.revertToApproved(command.failureReason());
        payrollRunRepository.save(run);

        return null;
    }
}
