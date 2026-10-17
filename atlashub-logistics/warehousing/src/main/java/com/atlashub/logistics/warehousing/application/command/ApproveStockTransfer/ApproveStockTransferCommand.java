package com.atlashub.logistics.warehousing.application.command.ApproveStockTransfer;

import com.atlashub.shared.domain.valueobject.Money;

public record ApproveStockTransferCommand(
        Long organizationId,
        Long transferId,
        Long approverId,
        Money declaredValue,
        String correlationId
) {}
