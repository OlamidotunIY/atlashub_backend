package com.atlashub.logistics.returns.application.command.CreateReturnShipment;

import com.atlashub.shared.domain.valueobject.Money;

public record CreateReturnShipmentResult(
        Long returnShipmentId,
        String returnTrackingNumber,
        String status,
        String faultType,
        Money customerRefundAmount,
        Money merchantPenaltyAmount,
        Money merchantCompensationAmount,
        boolean refundCustomerShipping
) {}
