package com.atlashub.logistics.returns.application.command.CreateReturnShipment;

import com.atlashub.logistics.returns.domain.valueobject.Address;
import com.atlashub.logistics.returns.domain.valueobject.ReturnFaultType;
import com.atlashub.logistics.returns.domain.valueobject.ReturnReasonType;
import com.atlashub.shared.domain.valueobject.Money;

public record CreateReturnShipmentCommand(
        Long organizationId,
        Long originalSalesOrderId,
        Long originalShipmentId,
        Long customerId,
        Address originAddress,
        Long destinationOutletId,
        ReturnReasonType reasonType,
        ReturnFaultType faultType,
        int deliveryAttemptsConfirmed,
        Money originalItemAmount,
        Money originalShippingFee,
        Money returnShippingFee,
        Money declaredValue,
        String correlationId
) {}
