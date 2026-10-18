package com.atlashub.logistics.shipping.application.commands.DispatchConsolidationBatch;

import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;

public record DispatchConsolidationBatchResult(
        Long batchId,
        String batchReference,
        String status,
        int shipmentCount,
        Money actualBatchCost,
        ZonedDateTime dispatchedAt
) {}
