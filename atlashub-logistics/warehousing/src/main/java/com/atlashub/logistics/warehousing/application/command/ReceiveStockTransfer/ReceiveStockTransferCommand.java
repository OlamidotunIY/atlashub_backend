package com.atlashub.logistics.warehousing.application.command.ReceiveStockTransfer;

import java.time.ZonedDateTime;

public record ReceiveStockTransferCommand(
        Long organizationId,
        Long transferId,
        ZonedDateTime receivedAt,
        String correlationId
) {}
