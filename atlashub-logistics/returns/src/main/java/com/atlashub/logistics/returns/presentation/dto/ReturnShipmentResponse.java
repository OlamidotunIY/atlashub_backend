package com.atlashub.logistics.returns.presentation.dto;

import com.atlashub.logistics.returns.domain.valueobject.Address;
import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;

public record ReturnShipmentResponse(
        Long id,
        Long organizationId,
        Long originalSalesOrderId,
        Long originalShipmentId,
        Long customerId,
        Address originAddress,
        Long destinationOutletId,
        String returnTrackingNumber,
        String reasonType,
        String faultType,
        int deliveryAttemptsConfirmed,
        Money originalItemAmount,
        Money originalShippingFee,
        Money returnShippingFee,
        Money declaredValue,
        Money customerRefundAmount,
        Money merchantPenaltyAmount,
        Money merchantCompensationAmount,
        boolean refundCustomerShipping,
        String status,
        String inspectionNotes,
        ZonedDateTime createdAt,
        ZonedDateTime receivedAt
) {}
