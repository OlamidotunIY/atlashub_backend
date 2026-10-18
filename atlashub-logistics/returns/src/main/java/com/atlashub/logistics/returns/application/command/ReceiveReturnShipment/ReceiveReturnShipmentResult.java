package com.atlashub.logistics.returns.application.command.ReceiveReturnShipment;

import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;

public record ReceiveReturnShipmentResult(
        Long returnShipmentId,
        String status,
        Money customerRefundAmount,
        Money merchantPenaltyAmount,
        Money merchantCompensationAmount,
        boolean refundCustomerShipping,
        ZonedDateTime receivedAt
) {}
