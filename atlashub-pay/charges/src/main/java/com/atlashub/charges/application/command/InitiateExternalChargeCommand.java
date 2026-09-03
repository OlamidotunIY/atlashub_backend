package com.atlashub.charges.application.command;

import com.atlashub.shared.domain.money.Money;

public record InitiateExternalChargeCommand(
        Long invoiceId,
        Long organizationId,
        Money amount,
        String customerEmail,
        String redirectUrl
) {}