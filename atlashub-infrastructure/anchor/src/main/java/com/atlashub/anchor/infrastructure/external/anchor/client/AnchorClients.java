package com.atlashub.anchor.infrastructure.external.anchor.client;

import java.util.Objects;

/**
 * Anchor resource clients sharing one environment-specific HTTP configuration.
 */
public record AnchorClients(
        AnchorDepositAccountClient depositAccounts,
        AnchorSubAccountClient subAccounts,
        AnchorReservedAccountClient reservedAccounts,
        AnchorBusinessCustomerClient businessCustomers,
        AnchorDocumentClient documents,
        AnchorWebhookClient webhooks
) {

    public AnchorClients {
        Objects.requireNonNull(depositAccounts, "Deposit-account client is required");
        Objects.requireNonNull(subAccounts, "Subaccount client is required");
        Objects.requireNonNull(reservedAccounts, "Reserved-account client is required");
        Objects.requireNonNull(businessCustomers, "Business-customer client is required");
        Objects.requireNonNull(documents, "Document client is required");
        Objects.requireNonNull(webhooks, "Webhook client is required");
    }
}
