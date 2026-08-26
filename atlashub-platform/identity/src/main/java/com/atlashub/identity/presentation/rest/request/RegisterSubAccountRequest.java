package com.atlashub.identity.presentation.rest.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterSubAccountRequest(
        @NotBlank String bankCode,
        @NotBlank String accountNumber,
        String description
) {}
