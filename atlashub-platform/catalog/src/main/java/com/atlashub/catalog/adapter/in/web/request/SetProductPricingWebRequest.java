package com.atlashub.catalog.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SetProductPricingWebRequest(
        @NotBlank String cycle,
        @NotNull BigDecimal amount,
        @NotBlank String currency
) {
}
