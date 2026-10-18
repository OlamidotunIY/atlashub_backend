package com.atlashub.hr.staff.application.command.AddEmployeeDeduction;

import com.atlashub.hr.staff.domain.valueobject.DeductionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddEmployeeDeductionCommand(
        Long employeeId,
        String name,
        DeductionType type,
        BigDecimal value,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        Long loanId
) {}
