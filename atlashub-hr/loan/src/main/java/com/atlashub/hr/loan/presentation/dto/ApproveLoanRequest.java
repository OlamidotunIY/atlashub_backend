package com.atlashub.hr.loan.presentation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ApproveLoanRequest(
        @NotNull LocalDate startDate
) {
}
