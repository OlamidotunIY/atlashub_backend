package com.atlashub.auth.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyMfaRequestDto(
        @NotBlank String preAuthToken,
        @NotBlank String code
) {}
