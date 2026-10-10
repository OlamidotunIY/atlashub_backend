package com.atlashub.commerce.storefront.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record KotItemRequest(
        @NotNull Long productId,
        @NotBlank String name,
        @Min(1) int quantity
) {
}
