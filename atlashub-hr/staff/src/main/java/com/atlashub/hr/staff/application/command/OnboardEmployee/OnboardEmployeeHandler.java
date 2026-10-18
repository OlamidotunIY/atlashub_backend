package com.atlashub.hr.staff.application.command.OnboardEmployee;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.exceptions.SalaryGradeNotFoundException;
import com.atlashub.hr.staff.domain.exceptions.UserAlreadyEmployedException;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.hr.staff.domain.repositories.SalaryGradeRepository;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.springframework.stereotype.Component;

@Component
public class OnboardEmployeeHandler extends Command<OnboardEmployeeCommand, OnboardEmployeeResponse> {

    private final EmployeeRepository employeeRepository;
    private final SalaryGradeRepository salaryGradeRepository;

    public OnboardEmployeeHandler(
            EmployeeRepository employeeRepository,
            SalaryGradeRepository salaryGradeRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.salaryGradeRepository = salaryGradeRepository;
    }

    @Override
    public OnboardEmployeeResponse execute(OnboardEmployeeCommand input) {
        if (employeeRepository.existsByOrganizationIdAndUserId(input.organizationId(), input.userId())) {
            throw new UserAlreadyEmployedException();
        }

        if (salaryGradeRepository.findById(input.salaryGradeId()).isEmpty()) {
            throw new SalaryGradeNotFoundException(input.salaryGradeId());
        }

        Long employeeId = employeeRepository.nextIdentity();
        String employeeNumber = String.format("EMP-%04d", employeeId);

        Employee employee = Employee.create(
                employeeId,
                input.organizationId(),
                input.userId(),
                employeeNumber,
                input.firstName(),
                input.lastName(),
                new EmailAddress(input.email()),
                new PhoneNumber(input.phone()),
                input.dateOfBirth(),
                input.address(),
                input.nationality(),
                input.hireDate(),
                input.designation(),
                input.department(),
                input.reportingManagerId(),
                input.salaryGradeId(),
                input.baseSalary()
        );

        employeeRepository.save(employee);
        return new OnboardEmployeeResponse(employeeId, employeeNumber);
    }
}
