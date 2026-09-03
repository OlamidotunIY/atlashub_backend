package com.atlashub.ledger.application.command;

import com.atlashub.shared.domain.money.CurrencyCode;
import java.math.BigDecimal;

public record HandleWalletChargeCommand(
        Long invoiceId,
        Long organizationId,
        BigDecimal amount,
        CurrencyCode currency
) {}
