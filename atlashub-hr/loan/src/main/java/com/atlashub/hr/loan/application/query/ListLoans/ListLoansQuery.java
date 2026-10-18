package com.atlashub.hr.loan.application.query.ListLoans;

import com.atlashub.hr.loan.domain.valueobject.LoanStatus;

public record ListLoansQuery(
        Long organizationId,
        Long employeeId,
        LoanStatus status
) {}
