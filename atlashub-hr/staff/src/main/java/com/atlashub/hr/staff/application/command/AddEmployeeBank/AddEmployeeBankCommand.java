package com.atlashub.hr.staff.application.command.AddEmployeeBank;

public record AddEmployeeBankCommand(
        Long employeeId,
        String bankCode,
        String bankName,
        String accountNumber,
        boolean setPrimary
) {}
