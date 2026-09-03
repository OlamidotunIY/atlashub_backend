package com.atlashub.charges.application.command;

public record HandlePaystackWebhookCommand(
        String event,
        String reference
) {}
