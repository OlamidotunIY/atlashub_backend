package com.atlashub.logistics.shipping.application.queries.GetShipmentDeliveryCode;

public record DeliveryCodeResult(
        Long shipmentId,
        String trackingNumber,
        String deliveryCode,
        String status
) {}
