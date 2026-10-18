package com.atlashub.logistics.warehousing.application.command.CancelStockTransfer;

public record CancelStockTransferCommand(
        Long organizationId,
        Long transferId,
        String reason,
        String correlationId
) {}
