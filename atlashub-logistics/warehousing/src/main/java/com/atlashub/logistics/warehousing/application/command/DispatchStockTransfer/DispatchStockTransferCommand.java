package com.atlashub.logistics.warehousing.application.command.DispatchStockTransfer;

public record DispatchStockTransferCommand(
        Long organizationId,
        Long transferId,
        Long shipmentId,
        String correlationId
) {}
