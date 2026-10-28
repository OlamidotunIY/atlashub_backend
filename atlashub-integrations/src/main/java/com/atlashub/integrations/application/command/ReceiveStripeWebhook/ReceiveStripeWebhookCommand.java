package com.atlashub.integrations.application.command.ReceiveStripeWebhook;

public record ReceiveStripeWebhookCommand(byte[] payload, String signature) {}
