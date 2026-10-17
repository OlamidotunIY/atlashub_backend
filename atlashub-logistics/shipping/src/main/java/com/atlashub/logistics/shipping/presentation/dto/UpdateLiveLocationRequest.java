package com.atlashub.logistics.shipping.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateLiveLocationRequest(
        String locationName,
        @NotNull BigDecimal latitude,
        @NotNull BigDecimal longitude
) {}
