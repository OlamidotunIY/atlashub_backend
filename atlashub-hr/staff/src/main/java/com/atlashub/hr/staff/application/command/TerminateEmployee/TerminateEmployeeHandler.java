package com.atlashub.hr.staff.application.command.TerminateEmployee;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.exceptions.EmployeeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class TerminateEmployeeHandler extends Command<TerminateEmployeeCommand, Void> {

    private final EmployeeRepository employeeRepository;

    public TerminateEmployeeHandler(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Void execute(TerminateEmployeeCommand input) {
        Employee employee = employeeRepository.findById(input.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(input.employeeId()));

        employee.terminate(input.terminationDate(), false);
        employeeRepository.save(employee);
        return null;
    }
}
