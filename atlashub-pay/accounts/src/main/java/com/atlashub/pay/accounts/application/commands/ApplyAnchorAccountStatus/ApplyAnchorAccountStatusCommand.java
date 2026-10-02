package com.atlashub.pay.accounts.application.commands.ApplyAnchorAccountStatus;

import com.atlashub.pay.accounts.domain.valueobject.ConfirmedBankingDetails;

public record ApplyAnchorAccountStatusCommand(
        String resourceType,
        String anchorResourceId,
        String status,
        ConfirmedBankingDetails details,
        String failureReason
) {
}
