package com.atlashub.hr.staff.application.command.AddEmployeeDeduction;

import com.atlashub.hr.staff.domain.entities.EmployeeDeduction;
import com.atlashub.hr.staff.domain.exceptions.EmployeeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.EmployeeDeductionRepository;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class AddEmployeeDeductionHandler extends Command<AddEmployeeDeductionCommand, Void> {

    private final EmployeeRepository employeeRepository;
    private final EmployeeDeductionRepository employeeDeductionRepository;

    public AddEmployeeDeductionHandler(
            EmployeeRepository employeeRepository,
            EmployeeDeductionRepository employeeDeductionRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.employeeDeductionRepository = employeeDeductionRepository;
    }

    @Override
    public Void execute(AddEmployeeDeductionCommand input) {
        if (employeeRepository.findById(input.employeeId()).isEmpty()) {
            throw new EmployeeNotFoundException(input.employeeId());
        }

        Long deductionId = employeeDeductionRepository.nextIdentity();
        EmployeeDeduction deduction = EmployeeDeduction.create(
                deductionId,
                input.employeeId(),
                input.name(),
                input.type(),
                input.value(),
                input.effectiveFrom(),
                input.effectiveTo(),
                input.loanId()
        );

        employeeDeductionRepository.save(deduction);
        return null;
    }
}
