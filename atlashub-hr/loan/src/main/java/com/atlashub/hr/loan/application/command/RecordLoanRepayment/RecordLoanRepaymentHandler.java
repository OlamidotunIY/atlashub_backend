package com.atlashub.hr.loan.application.command.RecordLoanRepayment;

import com.atlashub.hr.loan.domain.entities.EmployeeLoan;
import com.atlashub.hr.loan.domain.exceptions.LoanNotFoundException;
import com.atlashub.hr.loan.domain.repositories.EmployeeLoanRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RecordLoanRepaymentHandler extends Command<RecordLoanRepaymentCommand, Void> {

    private final EmployeeLoanRepository employeeLoanRepository;

    public RecordLoanRepaymentHandler(EmployeeLoanRepository employeeLoanRepository) {
        this.employeeLoanRepository = employeeLoanRepository;
    }

    @Override
    @Transactional
    public Void execute(RecordLoanRepaymentCommand command) {
        EmployeeLoan loan = employeeLoanRepository.findByIdWithLock(command.loanId())
                .orElseThrow(() -> new LoanNotFoundException(command.loanId()));

        loan.recordRepayment(command.amount());
        employeeLoanRepository.save(loan);

        return null;
    }
}
