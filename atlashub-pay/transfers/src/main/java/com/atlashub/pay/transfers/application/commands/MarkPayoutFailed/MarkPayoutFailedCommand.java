package com.atlashub.pay.transfers.application.commands.MarkPayoutFailed;

public record MarkPayoutFailedCommand(
        Long payoutId,
        String reason
) {
}
