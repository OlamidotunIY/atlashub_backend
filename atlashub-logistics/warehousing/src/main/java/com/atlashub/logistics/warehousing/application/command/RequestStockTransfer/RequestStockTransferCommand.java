package com.atlashub.logistics.warehousing.application.command.RequestStockTransfer;

import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.LocationType;
import com.atlashub.logistics.warehousing.domain.valueobject.StockTransferItem;
import com.atlashub.shared.domain.valueobject.Money;
import java.util.List;

public record RequestStockTransferCommand(
        Long organizationId,
        Long sourceId,
        LocationType sourceType,
        Address sourceAddress,
        Long destinationId,
        LocationType destinationType,
        Address destinationAddress,
        Money transferShippingFee,
        List<StockTransferItem> items,
        Long requestedBy,
        String correlationId
) {}
