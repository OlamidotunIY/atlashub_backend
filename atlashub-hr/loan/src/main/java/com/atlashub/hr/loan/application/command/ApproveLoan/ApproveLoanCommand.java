package com.atlashub.hr.loan.application.command.ApproveLoan;

import java.time.LocalDate;

public record ApproveLoanCommand(
        Long loanId,
        Long approverId,
        LocalDate startDate
) {}
