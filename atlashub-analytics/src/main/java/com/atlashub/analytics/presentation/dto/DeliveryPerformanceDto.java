package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;

public record DeliveryPerformanceDto(
        String month,
        int totalShipments,
        int delivered,
        int failed,
        int returned,
        BigDecimal deliverySuccessRate,
        int avgDeliveryMinutes
) {}
