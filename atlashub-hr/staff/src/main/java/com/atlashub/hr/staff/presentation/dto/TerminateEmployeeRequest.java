package com.atlashub.hr.staff.presentation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TerminateEmployeeRequest(
        @NotNull(message = "Termination date is required")
        LocalDate terminationDate
) {}
