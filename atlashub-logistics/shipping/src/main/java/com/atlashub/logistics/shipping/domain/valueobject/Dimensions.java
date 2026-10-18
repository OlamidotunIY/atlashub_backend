package com.atlashub.logistics.shipping.domain.valueobject;

import java.math.BigDecimal;

public record Dimensions(
        BigDecimal lengthCm,
        BigDecimal widthCm,
        BigDecimal heightCm
) {}
