package com.atlashub.pay.charges.application.commands.ReconcilePendingCharges;

import java.time.ZonedDateTime;

public record ReconcilePendingChargesCommand(
        ZonedDateTime createdBefore
) {
    public ReconcilePendingChargesCommand() {
        this(ZonedDateTime.now().minusMinutes(15));
    }
}
