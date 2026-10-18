package com.atlashub.hr.loan.application.command.ApplyForLoan;

import com.atlashub.shared.domain.valueobject.Money;

public record ApplyForLoanCommand(
        Long employeeId,
        Long organizationId,
        Money principalAmount,
        Money monthlyDeduction
) {}
