package com.atlashub.logistics.warehousing.application.command.CancelStockTransfer;

public record CancelStockTransferResult(
        Long transferId,
        String status,
        String reason
) {}
