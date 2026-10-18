package com.atlashub.hr.staff.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record AddEmployeeBankRequest(
        @NotBlank(message = "Bank code is required")
        String bankCode,
        @NotBlank(message = "Bank name is required")
        String bankName,
        @NotBlank(message = "Account number is required")
        String accountNumber,
        boolean setPrimary
) {}
