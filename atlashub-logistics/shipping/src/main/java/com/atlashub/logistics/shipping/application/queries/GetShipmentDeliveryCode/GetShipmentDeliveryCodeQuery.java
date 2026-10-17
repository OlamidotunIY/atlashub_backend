package com.atlashub.logistics.shipping.application.queries.GetShipmentDeliveryCode;

public record GetShipmentDeliveryCodeQuery(
        Long shipmentId,
        Long organizationId
) {}
