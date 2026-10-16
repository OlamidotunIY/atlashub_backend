package com.atlashub.hr.payroll.application.command.DisbursePayroll;

import com.atlashub.shared.application.port.PayrollReserveAccountPort;
import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.exceptions.InsufficientPayrollReserveException;
import com.atlashub.hr.payroll.domain.exceptions.InvalidPayrollStateException;
import com.atlashub.hr.payroll.domain.exceptions.PayrollRunNotFoundException;
import com.atlashub.hr.payroll.domain.repositories.PayrollRunRepository;
import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DisbursePayrollHandler extends Command<DisbursePayrollCommand, Void> {

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollReserveAccountPort payrollReserveAccountPort;

    public DisbursePayrollHandler(
            PayrollRunRepository payrollRunRepository,
            PayrollReserveAccountPort payrollReserveAccountPort
    ) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollReserveAccountPort = payrollReserveAccountPort;
    }

    @Override
    @Transactional
    public Void execute(DisbursePayrollCommand command) {
        PayrollRun run = payrollRunRepository.findById(command.payrollRunId())
                .orElseThrow(() -> new PayrollRunNotFoundException(command.payrollRunId()));

        if (run.getStatus() != PayrollStatus.APPROVED) {
            throw new InvalidPayrollStateException("Payroll must be in APPROVED state to disburse — current status: " + run.getStatus());
        }

        Money balance = payrollReserveAccountPort.getBalance(run.getOrganizationId());
        if (balance != null && run.getTotalNet() != null && balance.isLessThan(run.getTotalNet())) {
            throw new InsufficientPayrollReserveException();
        }

        run.markProcessing();
        payrollRunRepository.save(run);

        return null;
    }
}
