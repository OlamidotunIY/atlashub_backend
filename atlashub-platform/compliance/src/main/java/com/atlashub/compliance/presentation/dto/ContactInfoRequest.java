package com.atlashub.compliance.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactInfoRequest(
        @NotBlank @Email String supportEmail,
        @NotBlank @Email String disputeEmail,
        @NotBlank String whatsappNumber,
        @NotBlank String street,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String country
) {
}
