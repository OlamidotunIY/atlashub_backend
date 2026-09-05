package com.atlashub.ledger.application.command;

import java.math.BigDecimal;

public record ProcessWalletChargeCommand(Long invoiceId, Long organizationId, BigDecimal amount, String currency) {}
