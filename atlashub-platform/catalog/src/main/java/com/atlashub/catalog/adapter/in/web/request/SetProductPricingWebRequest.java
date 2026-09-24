package com.atlashub.catalog.adapter.in.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record SetProductPricingWebRequest(
        @NotBlank @Pattern(regexp = "MONTHLY|ANNUALLY") String cycle,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal amount,
        @NotBlank @Pattern(regexp = "NGN") String currency
) {
}
