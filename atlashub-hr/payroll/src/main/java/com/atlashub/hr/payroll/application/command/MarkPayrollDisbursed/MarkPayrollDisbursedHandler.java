package com.atlashub.hr.payroll.application.command.MarkPayrollDisbursed;

import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.exceptions.PayrollRunNotFoundException;
import com.atlashub.hr.payroll.domain.repositories.PayrollRunRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MarkPayrollDisbursedHandler extends Command<MarkPayrollDisbursedCommand, Void> {

    private final PayrollRunRepository payrollRunRepository;

    public MarkPayrollDisbursedHandler(PayrollRunRepository payrollRunRepository) {
        this.payrollRunRepository = payrollRunRepository;
    }

    @Override
    @Transactional
    public Void execute(MarkPayrollDisbursedCommand command) {
        PayrollRun run = payrollRunRepository.findById(command.payrollRunId())
                .orElseThrow(() -> new PayrollRunNotFoundException(command.payrollRunId()));

        run.markDisbursed();
        payrollRunRepository.save(run);

        return null;
    }
}
