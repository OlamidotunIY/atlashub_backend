package com.atlashub.hr.staff.application.command.SuspendEmployee;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.exceptions.EmployeeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class SuspendEmployeeHandler extends Command<SuspendEmployeeCommand, Void> {

    private final EmployeeRepository employeeRepository;

    public SuspendEmployeeHandler(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Void execute(SuspendEmployeeCommand input) {
        Employee employee = employeeRepository.findById(input.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(input.employeeId()));

        employee.suspend(input.reason());
        employeeRepository.save(employee);
        return null;
    }
}
