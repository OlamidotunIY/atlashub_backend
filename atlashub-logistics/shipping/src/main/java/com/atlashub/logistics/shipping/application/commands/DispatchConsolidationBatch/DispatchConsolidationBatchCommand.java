package com.atlashub.logistics.shipping.application.commands.DispatchConsolidationBatch;

import com.atlashub.logistics.shipping.domain.valueobject.CarrierChannel;
import com.atlashub.shared.domain.valueobject.Money;

public record DispatchConsolidationBatchCommand(
        Long batchId,
        CarrierChannel channel,
        String carrierCode,
        Money actualBatchCost,
        String correlationId
) {}
