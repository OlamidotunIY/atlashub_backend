package com.atlashub.logistics.warehousing.application.command.ReceiveStockTransfer;

import java.time.ZonedDateTime;

public record ReceiveStockTransferResult(
        Long transferId,
        String status,
        ZonedDateTime receivedAt
) {}
