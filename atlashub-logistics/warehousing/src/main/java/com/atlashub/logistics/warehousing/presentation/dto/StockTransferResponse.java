package com.atlashub.logistics.warehousing.presentation.dto;

import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.StockTransferItem;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public record StockTransferResponse(
        Long id,
        Long organizationId,
        Long sourceId,
        String sourceType,
        Address sourceAddress,
        Long destinationId,
        String destinationType,
        Address destinationAddress,
        BigDecimal totalWeightKg,
        Money transferShippingFee,
        String status,
        Long shipmentId,
        List<StockTransferItem> items,
        Long requestedBy,
        Long approvedBy,
        ZonedDateTime requestedAt,
        ZonedDateTime receivedAt
) {}
