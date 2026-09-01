package com.atlashub.auth.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDto(
        @NotBlank String jti
) {}
