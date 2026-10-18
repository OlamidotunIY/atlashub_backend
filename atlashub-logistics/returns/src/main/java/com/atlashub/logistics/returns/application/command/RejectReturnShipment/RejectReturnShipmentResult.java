package com.atlashub.logistics.returns.application.command.RejectReturnShipment;

public record RejectReturnShipmentResult(
        Long returnShipmentId,
        String status,
        String rejectionReason
) {}
