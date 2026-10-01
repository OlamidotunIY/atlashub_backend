package com.atlashub.accounts.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateOutletRequest(
        @NotBlank String name,
        String address,
        String city,
        String state,
        Long managerId
) {}
