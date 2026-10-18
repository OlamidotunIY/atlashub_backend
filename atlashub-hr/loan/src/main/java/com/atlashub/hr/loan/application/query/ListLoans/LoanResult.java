package com.atlashub.hr.loan.application.query.ListLoans;

import com.atlashub.hr.loan.domain.valueobject.LoanStatus;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record LoanResult(
        Long id,
        Long employeeId,
        Money principalAmount,
        Money outstandingBalance,
        Money monthlyDeduction,
        LoanStatus status,
        Long approvedBy,
        LocalDate startDate,
        ZonedDateTime disbursedAt
) {}
