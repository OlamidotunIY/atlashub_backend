package com.atlashub.hr.staff.application.command.LogInfraction;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.entities.Infraction;
import com.atlashub.hr.staff.domain.exceptions.EmployeeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.hr.staff.domain.repositories.InfractionRepository;
import com.atlashub.hr.staff.domain.valueobject.EmployeeStatus;
import com.atlashub.hr.staff.domain.valueobject.InfractionSeverity;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LogInfractionHandler extends Command<LogInfractionCommand, LogInfractionResponse> {

    private final EmployeeRepository employeeRepository;
    private final InfractionRepository infractionRepository;

    public LogInfractionHandler(
            EmployeeRepository employeeRepository,
            InfractionRepository infractionRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.infractionRepository = infractionRepository;
    }

    @Override
    public LogInfractionResponse execute(LogInfractionCommand input) {
        Optional<Employee> employeeOpt = employeeRepository.findById(input.employeeId());
        if (employeeOpt.isEmpty()) {
            throw new EmployeeNotFoundException(input.employeeId());
        }

        Employee employee = employeeOpt.get();
        if (input.severity() == InfractionSeverity.HIGH && employee.getStatus() == EmployeeStatus.ACTIVE) {
            employee.suspend("High severity infraction: " + input.description());
            employeeRepository.save(employee);
        }

        Long infractionId = infractionRepository.nextIdentity();
        Infraction infraction = Infraction.create(
                infractionId,
                input.employeeId(),
                input.organizationId(),
                input.description(),
                input.severity(),
                input.reportedBy()
        );

        infractionRepository.save(infraction);
        return new LogInfractionResponse(infractionId);
    }
}
