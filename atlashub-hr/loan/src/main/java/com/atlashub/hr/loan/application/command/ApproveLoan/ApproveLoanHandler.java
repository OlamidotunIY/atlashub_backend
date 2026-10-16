package com.atlashub.hr.loan.application.command.ApproveLoan;

import com.atlashub.hr.loan.domain.entities.EmployeeLoan;
import com.atlashub.hr.loan.domain.exceptions.LoanNotFoundException;
import com.atlashub.hr.loan.domain.repositories.EmployeeLoanRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApproveLoanHandler extends Command<ApproveLoanCommand, Void> {

    private final EmployeeLoanRepository employeeLoanRepository;

    public ApproveLoanHandler(EmployeeLoanRepository employeeLoanRepository) {
        this.employeeLoanRepository = employeeLoanRepository;
    }

    @Override
    @Transactional
    public Void execute(ApproveLoanCommand command) {
        EmployeeLoan loan = employeeLoanRepository.findById(command.loanId())
                .orElseThrow(() -> new LoanNotFoundException(command.loanId()));

        loan.approve(command.approverId(), command.startDate());
        employeeLoanRepository.save(loan);

        return null;
    }
}
