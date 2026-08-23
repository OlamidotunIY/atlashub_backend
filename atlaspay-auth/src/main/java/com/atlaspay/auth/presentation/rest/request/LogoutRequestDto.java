package com.atlaspay.auth.presentation.rest.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDto(
        @NotBlank String jti
) {}
