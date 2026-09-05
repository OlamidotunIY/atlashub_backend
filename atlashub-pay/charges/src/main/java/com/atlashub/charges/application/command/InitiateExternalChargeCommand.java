package com.atlashub.charges.application.command;

import com.atlashub.shared.domain.money.Money;

public record InitiateExternalChargeCommand(
        Long purposeId,
        Long organizationId,
        Money amount,
        String customerEmail,
        String redirectUrl
) {}
