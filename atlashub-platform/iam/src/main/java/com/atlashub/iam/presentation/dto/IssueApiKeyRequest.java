package com.atlashub.iam.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IssueApiKeyRequest(
        @NotBlank String name,
        @NotBlank String environment,
        @NotNull Long boundRoleId
) {
}
