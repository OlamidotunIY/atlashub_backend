package com.atlashub.pay.accounts.presentation.dto;

import com.atlashub.pay.accounts.domain.valueobject.ReservedAccountOwnerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IssueReservedAccountRequest(
        @NotNull ReservedAccountOwnerType ownerType,
        @NotBlank String ownerReferenceId,
        @NotBlank String customerType,
        @NotBlank String customerReferenceId,
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String bvn,
        @NotBlank String provider
) {
}
