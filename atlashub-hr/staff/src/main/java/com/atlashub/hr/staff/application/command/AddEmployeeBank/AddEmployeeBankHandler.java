package com.atlashub.hr.staff.application.command.AddEmployeeBank;

import com.atlashub.shared.application.port.BankVerificationPort;
import com.atlashub.hr.staff.domain.entities.EmployeeBank;
import com.atlashub.hr.staff.domain.exceptions.EmployeeBankUnverifiedException;
import com.atlashub.hr.staff.domain.exceptions.EmployeeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.EmployeeBankRepository;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AddEmployeeBankHandler extends Command<AddEmployeeBankCommand, AddEmployeeBankResponse> {

    private final EmployeeRepository employeeRepository;
    private final EmployeeBankRepository employeeBankRepository;
    private final BankVerificationPort bankVerificationPort;

    public AddEmployeeBankHandler(
            EmployeeRepository employeeRepository,
            EmployeeBankRepository employeeBankRepository,
            BankVerificationPort bankVerificationPort
    ) {
        this.employeeRepository = employeeRepository;
        this.employeeBankRepository = employeeBankRepository;
        this.bankVerificationPort = bankVerificationPort;
    }

    @Override
    public AddEmployeeBankResponse execute(AddEmployeeBankCommand input) {
        if (employeeRepository.findById(input.employeeId()).isEmpty()) {
            throw new EmployeeNotFoundException(input.employeeId());
        }

        String resolvedAccountName = bankVerificationPort.verifyAccount(input.bankCode(), input.accountNumber());
        if (resolvedAccountName == null || resolvedAccountName.isBlank()) {
            throw new EmployeeBankUnverifiedException("Bank account could not be verified");
        }

        if (input.setPrimary()) {
            Optional<EmployeeBank> existingPrimary = employeeBankRepository.findByEmployeeIdAndIsPrimaryTrue(input.employeeId());
            existingPrimary.ifPresent(bank -> {
                bank.setPrimary(false);
                employeeBankRepository.save(bank);
            });
        }

        Long bankId = employeeBankRepository.nextIdentity();
        EmployeeBank bank = EmployeeBank.create(
                bankId,
                input.employeeId(),
                input.bankCode(),
                input.bankName(),
                input.accountNumber(),
                resolvedAccountName,
                true,
                input.setPrimary()
        );

        employeeBankRepository.save(bank);
        return new AddEmployeeBankResponse(bankId, resolvedAccountName);
    }
}
