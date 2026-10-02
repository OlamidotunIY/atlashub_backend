package com.atlashub.compliance.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record OwnerIdentityRequest(
        @NotBlank String bvn,
        String nin,
        @NotNull LocalDate dateOfBirth,
        @NotBlank String governmentIdType,
        @NotBlank String governmentIdNumber,
        @NotBlank String governmentIdFrontUrl,
        @NotBlank String governmentIdBackUrl,
        @NotBlank String selfieUrl
) {
}
