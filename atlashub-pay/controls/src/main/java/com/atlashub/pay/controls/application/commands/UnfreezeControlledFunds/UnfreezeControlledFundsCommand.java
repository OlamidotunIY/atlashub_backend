package com.atlashub.pay.controls.application.commands.UnfreezeControlledFunds;

import com.atlashub.shared.domain.valueobject.Money;

public record UnfreezeControlledFundsCommand(
        Long facilityId,
        Long organizationId,
        String accountReference,
        Money amount,
        String rail,
        String providerControlReference,
        String reason
) {}
