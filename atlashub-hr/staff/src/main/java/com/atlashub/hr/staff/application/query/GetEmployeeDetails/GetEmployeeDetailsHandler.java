package com.atlashub.hr.staff.application.query.GetEmployeeDetails;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.exceptions.EmployeeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.EmployeeAllowanceRepository;
import com.atlashub.hr.staff.domain.repositories.EmployeeBankRepository;
import com.atlashub.hr.staff.domain.repositories.EmployeeDeductionRepository;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetEmployeeDetailsHandler extends Query<GetEmployeeDetailsQuery, EmployeeDetailsResult> {

    private final EmployeeRepository employeeRepository;
    private final EmployeeBankRepository employeeBankRepository;
    private final EmployeeAllowanceRepository employeeAllowanceRepository;
    private final EmployeeDeductionRepository employeeDeductionRepository;

    public GetEmployeeDetailsHandler(
            EmployeeRepository employeeRepository,
            EmployeeBankRepository employeeBankRepository,
            EmployeeAllowanceRepository employeeAllowanceRepository,
            EmployeeDeductionRepository employeeDeductionRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.employeeBankRepository = employeeBankRepository;
        this.employeeAllowanceRepository = employeeAllowanceRepository;
        this.employeeDeductionRepository = employeeDeductionRepository;
    }

    @Override
    public EmployeeDetailsResult execute(GetEmployeeDetailsQuery query) {
        Employee employee = employeeRepository.findById(query.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(query.employeeId()));

        if (query.organizationId() != null && !query.organizationId().equals(employee.getOrganizationId())) {
            throw new EmployeeNotFoundException(query.employeeId());
        }

        List<EmployeeDetailsResult.BankDetail> banks = employeeBankRepository.findByEmployeeId(employee.getId())
                .stream()
                .map(b -> new EmployeeDetailsResult.BankDetail(
                        b.getId(),
                        b.getBankCode(),
                        b.getBankName(),
                        b.getAccountNumber(),
                        b.getAccountName(),
                        b.isVerified(),
                        b.isPrimary()
                ))
                .toList();

        List<EmployeeDetailsResult.AllowanceDetail> allowances = employeeAllowanceRepository.findByEmployeeId(employee.getId())
                .stream()
                .map(a -> new EmployeeDetailsResult.AllowanceDetail(
                        a.getId(),
                        a.getName(),
                        a.getType(),
                        a.getValue(),
                        a.getEffectiveFrom(),
                        a.getEffectiveTo()
                ))
                .toList();

        List<EmployeeDetailsResult.DeductionDetail> deductions = employeeDeductionRepository.findByEmployeeId(employee.getId())
                .stream()
                .map(d -> new EmployeeDetailsResult.DeductionDetail(
                        d.getId(),
                        d.getName(),
                        d.getType(),
                        d.getValue(),
                        d.getEffectiveFrom(),
                        d.getEffectiveTo(),
                        d.getLoanId()
                ))
                .toList();

        return new EmployeeDetailsResult(
                employee.getId(),
                employee.getOrganizationId(),
                employee.getUserId(),
                employee.getEmployeeNumber(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail().value(),
                employee.getPhone() != null ? employee.getPhone().value() : null,
                employee.getDateOfBirth(),
                employee.getAddress(),
                employee.getNationality(),
                employee.getHireDate(),
                employee.getTerminationDate(),
                employee.getDesignation(),
                employee.getDepartment(),
                employee.getReportingManagerId(),
                employee.getSalaryGradeId(),
                employee.getBaseSalary().amount(),
                employee.getBaseSalary().currency().name(),
                employee.getStatus(),
                banks,
                allowances,
                deductions
        );
    }
}
