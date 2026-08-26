package com.atlashub.identity.adapter.in.web.request;

import java.math.BigDecimal;

public record AnnualProjectedSalesVolumeDto(
    BigDecimal amount,
    String currency
) {}
