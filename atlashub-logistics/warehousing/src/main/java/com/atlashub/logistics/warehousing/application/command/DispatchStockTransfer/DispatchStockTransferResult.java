package com.atlashub.logistics.warehousing.application.command.DispatchStockTransfer;

public record DispatchStockTransferResult(
        Long transferId,
        String status,
        Long shipmentId
) {}
