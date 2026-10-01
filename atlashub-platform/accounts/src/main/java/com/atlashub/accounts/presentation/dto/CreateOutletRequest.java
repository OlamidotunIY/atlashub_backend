package com.atlashub.accounts.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOutletRequest(
        @NotNull @Positive Long organizationId,
        @NotBlank String name,
        String address,
        String city,
        String state,
        @NotBlank String country,
        Long managerId
) {}
