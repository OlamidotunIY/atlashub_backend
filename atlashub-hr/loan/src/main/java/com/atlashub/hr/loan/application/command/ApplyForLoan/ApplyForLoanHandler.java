package com.atlashub.hr.loan.application.command.ApplyForLoan;

import com.atlashub.hr.loan.domain.entities.EmployeeLoan;
import com.atlashub.hr.loan.domain.exceptions.LoanAlreadyActiveException;
import com.atlashub.hr.loan.domain.repositories.EmployeeLoanRepository;
import com.atlashub.hr.loan.domain.valueobject.LoanStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ApplyForLoanHandler extends Command<ApplyForLoanCommand, ApplyForLoanResponse> {

    private final EmployeeLoanRepository employeeLoanRepository;

    public ApplyForLoanHandler(EmployeeLoanRepository employeeLoanRepository) {
        this.employeeLoanRepository = employeeLoanRepository;
    }

    @Override
    @Transactional
    public ApplyForLoanResponse execute(ApplyForLoanCommand command) {
        List<EmployeeLoan> existingLoans = employeeLoanRepository.findByEmployeeIdAndStatusIn(
                command.employeeId(),
                List.of(LoanStatus.ACTIVE, LoanStatus.APPROVED, LoanStatus.PENDING_APPROVAL)
        );

        if (!existingLoans.isEmpty()) {
            throw new LoanAlreadyActiveException();
        }

        EmployeeLoan loan = EmployeeLoan.create(
                null,
                command.employeeId(),
                command.organizationId(),
                command.principalAmount(),
                command.monthlyDeduction()
        );

        EmployeeLoan saved = employeeLoanRepository.save(loan);
        return new ApplyForLoanResponse(saved.getId());
    }
}
