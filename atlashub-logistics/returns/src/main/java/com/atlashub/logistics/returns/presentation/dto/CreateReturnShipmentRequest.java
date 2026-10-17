package com.atlashub.logistics.returns.presentation.dto;

import com.atlashub.logistics.returns.domain.valueobject.Address;
import com.atlashub.logistics.returns.domain.valueobject.ReturnFaultType;
import com.atlashub.logistics.returns.domain.valueobject.ReturnReasonType;
import com.atlashub.shared.domain.valueobject.Money;
import jakarta.validation.constraints.NotNull;

public record CreateReturnShipmentRequest(
        @NotNull(message = "Original sales order ID is required")
        Long originalSalesOrderId,

        Long originalShipmentId,

        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotNull(message = "Origin address is required")
        Address originAddress,

        Long destinationOutletId,

        @NotNull(message = "Return reason type is required")
        ReturnReasonType reasonType,

        @NotNull(message = "Return fault type is required")
        ReturnFaultType faultType,

        int deliveryAttemptsConfirmed,

        @NotNull(message = "Original item amount is required")
        Money originalItemAmount,

        @NotNull(message = "Original shipping fee is required")
        Money originalShippingFee,

        @NotNull(message = "Return shipping fee is required")
        Money returnShippingFee,

        @NotNull(message = "Declared value is required")
        Money declaredValue
) {}
