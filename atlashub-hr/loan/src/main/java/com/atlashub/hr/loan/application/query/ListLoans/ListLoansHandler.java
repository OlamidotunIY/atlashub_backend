package com.atlashub.hr.loan.application.query.ListLoans;

import com.atlashub.hr.loan.domain.entities.EmployeeLoan;
import com.atlashub.hr.loan.domain.repositories.EmployeeLoanRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListLoansHandler extends Query<ListLoansQuery, List<LoanResult>> {

    private final EmployeeLoanRepository employeeLoanRepository;

    public ListLoansHandler(EmployeeLoanRepository employeeLoanRepository) {
        this.employeeLoanRepository = employeeLoanRepository;
    }

    @Override
    public List<LoanResult> execute(ListLoansQuery query) {
        List<EmployeeLoan> loans;

        if (query.organizationId() != null && query.employeeId() != null && query.status() != null) {
            loans = employeeLoanRepository.findByOrganizationIdAndEmployeeIdAndStatus(
                    query.organizationId(),
                    query.employeeId(),
                    query.status()
            );
        } else if (query.organizationId() != null && query.employeeId() != null) {
            loans = employeeLoanRepository.findByOrganizationIdAndEmployeeId(
                    query.organizationId(),
                    query.employeeId()
            );
        } else if (query.employeeId() != null && query.status() != null) {
            loans = employeeLoanRepository.findByEmployeeIdAndStatus(
                    query.employeeId(),
                    query.status()
            );
        } else if (query.organizationId() != null && query.status() != null) {
            loans = employeeLoanRepository.findByOrganizationIdAndStatus(
                    query.organizationId(),
                    query.status()
            );
        } else if (query.organizationId() != null) {
            loans = employeeLoanRepository.findByOrganizationId(query.organizationId());
        } else {
            loans = List.of();
        }

        return loans.stream()
                .map(this::toResult)
                .toList();
    }

    private LoanResult toResult(EmployeeLoan loan) {
        return new LoanResult(
                loan.getId(),
                loan.getEmployeeId(),
                loan.getPrincipalAmount(),
                loan.getOutstandingBalance(),
                loan.getMonthlyDeduction(),
                loan.getStatus(),
                loan.getApprovedBy(),
                loan.getStartDate(),
                loan.getDisbursedAt()
        );
    }
}
