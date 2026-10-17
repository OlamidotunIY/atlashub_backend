package com.atlashub.logistics.shipping.application.queries.ListShipments;

public record ListShipmentsQuery(
        Long organizationId,
        String status
) {}
