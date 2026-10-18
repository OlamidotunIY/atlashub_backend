package com.atlashub.hr.staff.application.command.PromoteEmployee;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.exceptions.EmployeeNotFoundException;
import com.atlashub.hr.staff.domain.exceptions.SalaryGradeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.hr.staff.domain.repositories.SalaryGradeRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class PromoteEmployeeHandler extends Command<PromoteEmployeeCommand, Void> {

    private final EmployeeRepository employeeRepository;
    private final SalaryGradeRepository salaryGradeRepository;

    public PromoteEmployeeHandler(
            EmployeeRepository employeeRepository,
            SalaryGradeRepository salaryGradeRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.salaryGradeRepository = salaryGradeRepository;
    }

    @Override
    public Void execute(PromoteEmployeeCommand input) {
        Employee employee = employeeRepository.findById(input.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(input.employeeId()));

        if (salaryGradeRepository.findById(input.newGradeId()).isEmpty()) {
            throw new SalaryGradeNotFoundException(input.newGradeId());
        }

        employee.promote(input.newDesignation(), input.newGradeId(), input.newBaseSalary());
        employeeRepository.save(employee);
        return null;
    }
}
