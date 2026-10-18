package com.atlashub.logistics.warehousing.presentation.dto;

import java.time.ZonedDateTime;

public record ReceiveStockTransferRequest(
        ZonedDateTime receivedAt
) {}
