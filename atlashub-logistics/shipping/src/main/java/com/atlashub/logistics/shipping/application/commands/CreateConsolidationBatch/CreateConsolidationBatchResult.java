package com.atlashub.logistics.shipping.application.commands.CreateConsolidationBatch;

import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public record CreateConsolidationBatchResult(
        Long batchId,
        String batchReference,
        String routeType,
        List<Long> shipmentIds,
        BigDecimal totalWeightKg,
        Money totalBaselineCost,
        ZonedDateTime latestDispatchAt
) {}
