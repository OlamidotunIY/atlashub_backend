package com.atlashub.pay.controls.application.commands.FreezeControlledFunds;

import com.atlashub.shared.domain.valueobject.Money;

public record FreezeControlledFundsCommand(
        Long facilityId,
        Long organizationId,
        String accountReference,
        Money amount,
        String rail,
        String reason
) {}
