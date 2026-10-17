package com.atlashub.logistics.shipping.application.queries.GetShipmentDetails;

public record GetShipmentDetailsQuery(
        Long shipmentId,
        Long organizationId
) {}
