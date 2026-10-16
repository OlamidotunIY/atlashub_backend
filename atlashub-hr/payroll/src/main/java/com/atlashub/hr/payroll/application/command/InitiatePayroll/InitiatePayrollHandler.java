package com.atlashub.hr.payroll.application.command.InitiatePayroll;

import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.entities.Payslip;
import com.atlashub.hr.payroll.domain.exceptions.DuplicatePayrollPeriodException;
import com.atlashub.hr.payroll.domain.exceptions.InsufficientApproversException;
import com.atlashub.hr.payroll.domain.repositories.PayrollRunRepository;
import com.atlashub.hr.payroll.domain.services.PayrollCalculationService;
import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import com.atlashub.shared.application.port.MembershipQueryPort;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class InitiatePayrollHandler extends Command<InitiatePayrollCommand, InitiatePayrollResponse> {

    private final PayrollRunRepository payrollRunRepository;
    private final MembershipQueryPort membershipQueryPort;
    private final PayrollCalculationService payrollCalculationService;

    public InitiatePayrollHandler(
            PayrollRunRepository payrollRunRepository,
            MembershipQueryPort membershipQueryPort,
            PayrollCalculationService payrollCalculationService
    ) {
        this.payrollRunRepository = payrollRunRepository;
        this.membershipQueryPort = membershipQueryPort;
        this.payrollCalculationService = payrollCalculationService;
    }

    @Override
    @Transactional
    public InitiatePayrollResponse execute(InitiatePayrollCommand command) {
        int approversCount = membershipQueryPort.countMembersWithPermission(
                command.organizationId(),
                "hr:payroll:approve"
        );
        if (approversCount < 2) {
            throw new InsufficientApproversException();
        }

        payrollRunRepository.findByOrganizationIdAndPeriod(command.organizationId(), command.period())
                .ifPresent(run -> {
                    if (run.getStatus() != PayrollStatus.FAILED) {
                        throw new DuplicatePayrollPeriodException(command.period().toString());
                    }
                });

        List<Payslip> payslips = payrollCalculationService.calculatePayroll(
                command.organizationId(),
                command.period()
        );

        PayrollRun run = PayrollRun.create(
                null,
                command.organizationId(),
                command.period(),
                command.initiatorUserId(),
                payslips
        );

        PayrollRun saved = payrollRunRepository.save(run);
        return new InitiatePayrollResponse(saved.getId());
    }
}
