package com.atlashub.logistics.returns.presentation.dto;

import java.time.ZonedDateTime;

public record ReceiveReturnShipmentRequest(
        String inspectionNotes,
        ZonedDateTime receivedAt
) {}
