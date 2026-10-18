package com.atlashub.hr.staff.application.command.ReinstateEmployee;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.exceptions.EmployeeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class ReinstateEmployeeHandler extends Command<ReinstateEmployeeCommand, Void> {

    private final EmployeeRepository employeeRepository;

    public ReinstateEmployeeHandler(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Void execute(ReinstateEmployeeCommand input) {
        Employee employee = employeeRepository.findById(input.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(input.employeeId()));

        employee.reinstate();
        employeeRepository.save(employee);
        return null;
    }
}
