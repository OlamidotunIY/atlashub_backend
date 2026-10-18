package com.atlashub.analytics.application.query.GetDeliveryPerformance;

import java.math.BigDecimal;

public record DeliveryPerformanceResult(
        String month,
        int totalShipments,
        int delivered,
        int failed,
        int returned,
        BigDecimal deliverySuccessRate,
        int avgDeliveryMinutes
) {}
