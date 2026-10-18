package com.atlashub.logistics.shipping.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record Route3PLRequest(
        @NotBlank String carrierCode
) {}
