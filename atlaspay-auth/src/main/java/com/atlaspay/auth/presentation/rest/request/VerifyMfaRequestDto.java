package com.atlaspay.auth.presentation.rest.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyMfaRequestDto(
        @NotBlank String preAuthToken,
        @NotBlank String code
) {}
