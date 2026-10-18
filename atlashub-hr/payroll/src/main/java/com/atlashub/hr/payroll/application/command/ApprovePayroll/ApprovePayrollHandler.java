package com.atlashub.hr.payroll.application.command.ApprovePayroll;

import com.atlashub.hr.payroll.application.command.DisbursePayroll.DisbursePayrollCommand;
import com.atlashub.hr.payroll.application.command.DisbursePayroll.DisbursePayrollHandler;
import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.exceptions.PayrollRunNotFoundException;
import com.atlashub.hr.payroll.domain.repositories.PayrollRunRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApprovePayrollHandler extends Command<ApprovePayrollCommand, Void> {

    private final PayrollRunRepository payrollRunRepository;
    private final DisbursePayrollHandler disbursePayrollHandler;

    public ApprovePayrollHandler(
            PayrollRunRepository payrollRunRepository,
            DisbursePayrollHandler disbursePayrollHandler
    ) {
        this.payrollRunRepository = payrollRunRepository;
        this.disbursePayrollHandler = disbursePayrollHandler;
    }

    @Override
    @Transactional
    public Void execute(ApprovePayrollCommand command) {
        PayrollRun run = payrollRunRepository.findById(command.payrollRunId())
                .orElseThrow(() -> new PayrollRunNotFoundException(command.payrollRunId()));

        run.approve(command.approverUserId());
        payrollRunRepository.save(run);

        disbursePayrollHandler.execute(new DisbursePayrollCommand(run.getId()));

        return null;
    }
}
