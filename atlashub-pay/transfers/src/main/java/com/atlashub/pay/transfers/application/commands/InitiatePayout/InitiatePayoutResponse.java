package com.atlashub.pay.transfers.application.commands.InitiatePayout;

public record InitiatePayoutResponse(
        Long payoutId,
        String reference,
        String status,
        boolean requiresApproval
) {
}
