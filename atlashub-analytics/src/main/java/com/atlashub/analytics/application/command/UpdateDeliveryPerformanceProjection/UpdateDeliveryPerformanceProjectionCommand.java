package com.atlashub.analytics.application.command.UpdateDeliveryPerformanceProjection;

public record UpdateDeliveryPerformanceProjectionCommand(
        Long organizationId,
        String environment,
        String month,
        boolean isDelivered,
        boolean isReturned,
        Integer deliveryMinutes
) {}
