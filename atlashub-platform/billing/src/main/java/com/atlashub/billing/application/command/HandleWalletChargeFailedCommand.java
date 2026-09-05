package com.atlashub.billing.application.command;
public record HandleWalletChargeFailedCommand(Long invoiceId, String reason) {}
