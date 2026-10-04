package com.atlashub.iam.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record IssueApiKeyRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 10) String environment,
        @Positive Long boundRoleId
) {
}
