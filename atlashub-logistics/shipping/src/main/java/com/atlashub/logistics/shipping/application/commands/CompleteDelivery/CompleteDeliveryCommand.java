package com.atlashub.logistics.shipping.application.commands.CompleteDelivery;

public record CompleteDeliveryCommand(
        Long shipmentId,
        String deliveryCode,
        String proofOfDeliveryUrl,
        String correlationId
) {}
