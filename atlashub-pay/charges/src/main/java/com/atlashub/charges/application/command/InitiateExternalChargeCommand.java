package com.atlashub.charges.application.command;

import java.math.BigDecimal;

public record InitiateExternalChargeCommand(
        Long invoiceId,
        Long organizationId,
        BigDecimal amount,
        String currency,
        String customerEmail,
        String redirectUrl
) {}