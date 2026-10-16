package com.atlashub.pay.transfers.application.commands.ApprovePayout;

public record ApprovePayoutCommand(
        Long payoutId,
        Long approverUserId
) {
}
