package com.atlashub.logistics.shipping.application.commands.CreateConsolidationBatch;

import java.util.List;

public record CreateConsolidationBatchCommand(
        String originState,
        String originCity,
        String destinationState,
        String destinationCity,
        List<Long> shipmentIds,
        String currency
) {}
