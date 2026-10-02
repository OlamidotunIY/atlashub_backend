package com.atlashub.accounts.presentation.dto;

import jakarta.validation.constraints.NotBlank;
public record CreateOutletRequest(
        @NotBlank String name,
        String address,
        String city,
        String state,
        Long managerId
) {}
