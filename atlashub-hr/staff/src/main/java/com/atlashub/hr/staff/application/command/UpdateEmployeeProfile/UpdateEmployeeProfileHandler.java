package com.atlashub.hr.staff.application.command.UpdateEmployeeProfile;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.exceptions.EmployeeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.springframework.stereotype.Component;

@Component
public class UpdateEmployeeProfileHandler extends Command<UpdateEmployeeProfileCommand, Void> {

    private final EmployeeRepository employeeRepository;

    public UpdateEmployeeProfileHandler(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Void execute(UpdateEmployeeProfileCommand input) {
        Employee employee = employeeRepository.findById(input.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(input.employeeId()));

        PhoneNumber phone = input.phone() != null && !input.phone().isBlank()
                ? new PhoneNumber(input.phone())
                : null;

        employee.updateBioData(
                input.firstName(),
                input.lastName(),
                phone,
                input.address(),
                input.nationality()
        );

        employeeRepository.save(employee);
        return null;
    }
}
