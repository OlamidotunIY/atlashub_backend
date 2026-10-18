package com.atlashub.logistics.warehousing.presentation.dto;

import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.LocationType;
import com.atlashub.logistics.warehousing.domain.valueobject.StockTransferItem;
import com.atlashub.shared.domain.valueobject.Money;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RequestStockTransferRequest(
        @NotNull(message = "Source ID is required")
        Long sourceId,

        @NotNull(message = "Source location type is required")
        LocationType sourceType,

        @NotNull(message = "Source address is required")
        Address sourceAddress,

        @NotNull(message = "Destination ID is required")
        Long destinationId,

        @NotNull(message = "Destination location type is required")
        LocationType destinationType,

        @NotNull(message = "Destination address is required")
        Address destinationAddress,

        @NotNull(message = "Transfer shipping fee is required")
        Money transferShippingFee,

        @NotEmpty(message = "Stock transfer must contain at least one item")
        List<StockTransferItem> items
) {}
