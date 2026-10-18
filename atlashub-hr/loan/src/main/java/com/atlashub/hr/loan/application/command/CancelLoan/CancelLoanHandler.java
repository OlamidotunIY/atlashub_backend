package com.atlashub.hr.loan.application.command.CancelLoan;

import com.atlashub.hr.loan.domain.entities.EmployeeLoan;
import com.atlashub.hr.loan.domain.exceptions.LoanNotFoundException;
import com.atlashub.hr.loan.domain.repositories.EmployeeLoanRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CancelLoanHandler extends Command<CancelLoanCommand, Void> {

    private final EmployeeLoanRepository employeeLoanRepository;

    public CancelLoanHandler(EmployeeLoanRepository employeeLoanRepository) {
        this.employeeLoanRepository = employeeLoanRepository;
    }

    @Override
    @Transactional
    public Void execute(CancelLoanCommand command) {
        EmployeeLoan loan = employeeLoanRepository.findById(command.loanId())
                .orElseThrow(() -> new LoanNotFoundException(command.loanId()));

        loan.cancel();
        employeeLoanRepository.save(loan);

        return null;
    }
}
