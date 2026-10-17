package com.atlashub.logistics.shipping.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CalculateIncentivesRequest(
        @NotBlank String billingYearMonth
) {}
