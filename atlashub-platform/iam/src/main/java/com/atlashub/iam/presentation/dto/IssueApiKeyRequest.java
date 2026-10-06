package com.atlashub.iam.presentation.dto;

import com.atlashub.shared.application.security.ApiEnvironment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record IssueApiKeyRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull ApiEnvironment environment,
        @NotNull @Positive Long boundRoleId
) {
}
