package com.atlashub.pay.accounts.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record IssueReservedAccountRequest(
        @NotBlank String ownerType,
        @NotBlank String ownerReferenceId,
        @NotBlank String customerType,
        @NotBlank String customerReferenceId,
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String bvn,
        @NotBlank String provider
) {
}
