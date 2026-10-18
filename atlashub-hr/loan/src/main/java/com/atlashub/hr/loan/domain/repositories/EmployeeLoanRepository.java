package com.atlashub.hr.loan.domain.repositories;

import com.atlashub.hr.loan.domain.entities.EmployeeLoan;
import com.atlashub.hr.loan.domain.valueobject.LoanStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface EmployeeLoanRepository extends Repository<EmployeeLoan> {

    List<EmployeeLoan> findByEmployeeIdAndStatus(Long employeeId, LoanStatus status);

    List<EmployeeLoan> findByEmployeeIdAndStatusIn(Long employeeId, List<LoanStatus> statuses);

    Optional<EmployeeLoan> findByIdWithLock(Long loanId);

    List<EmployeeLoan> findByOrganizationId(Long organizationId);

    List<EmployeeLoan> findByOrganizationIdAndStatus(Long organizationId, LoanStatus status);

    List<EmployeeLoan> findByOrganizationIdAndEmployeeIdAndStatus(Long organizationId, Long employeeId, LoanStatus status);

    List<EmployeeLoan> findByOrganizationIdAndEmployeeId(Long organizationId, Long employeeId);
}
