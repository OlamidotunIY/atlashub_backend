package com.atlashub.compliance.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ContactInfoRequest(
        @NotBlank @Email String generalEmail,
        @NotBlank @Email String supportEmail,
        @NotBlank @Email String disputeEmail,
        @NotBlank String phoneNumber,
        @NotNull @Valid AddressRequest mainAddress,
        @NotNull @Valid AddressRequest registeredAddress
) {
}
