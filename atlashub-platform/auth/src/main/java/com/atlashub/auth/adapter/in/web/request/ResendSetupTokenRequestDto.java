package com.atlashub.auth.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record ResendSetupTokenRequestDto(
        @NotBlank(message = "Identifier is required")
        String identifier
) {}