package com.atlashub.compliance.presentation.dto;
import jakarta.validation.constraints.NotBlank;
public record AddressRequest(@NotBlank String addressLine1, String addressLine2, @NotBlank String city,
        @NotBlank String state, @NotBlank String postalCode, @NotBlank String country) {}
