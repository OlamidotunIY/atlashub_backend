package com.atlaspay.auth.presentation.rest.request;

import jakarta.validation.constraints.NotBlank;

public record ResendSetupTokenRequestDto(
        @NotBlank(message = "Identifier is required")
        String identifier
) {}