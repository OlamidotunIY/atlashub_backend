package com.atlashub.identity.presentation.rest.request;

import java.math.BigDecimal;

public record AnnualProjectedSalesVolumeDto(
    BigDecimal amount,
    String currency
) {}
