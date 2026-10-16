package com.atlashub.hr.staff.application.command.AddEmployeeAllowance;

import com.atlashub.hr.staff.domain.entities.EmployeeAllowance;
import com.atlashub.hr.staff.domain.exceptions.EmployeeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.EmployeeAllowanceRepository;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class AddEmployeeAllowanceHandler extends Command<AddEmployeeAllowanceCommand, Void> {

    private final EmployeeRepository employeeRepository;
    private final EmployeeAllowanceRepository employeeAllowanceRepository;

    public AddEmployeeAllowanceHandler(
            EmployeeRepository employeeRepository,
            EmployeeAllowanceRepository employeeAllowanceRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.employeeAllowanceRepository = employeeAllowanceRepository;
    }

    @Override
    public Void execute(AddEmployeeAllowanceCommand input) {
        if (employeeRepository.findById(input.employeeId()).isEmpty()) {
            throw new EmployeeNotFoundException(input.employeeId());
        }

        Long allowanceId = employeeAllowanceRepository.nextIdentity();
        EmployeeAllowance allowance = EmployeeAllowance.create(
                allowanceId,
                input.employeeId(),
                input.name(),
                input.type(),
                input.value(),
                input.effectiveFrom(),
                input.effectiveTo()
        );

        employeeAllowanceRepository.save(allowance);
        return null;
    }
}
