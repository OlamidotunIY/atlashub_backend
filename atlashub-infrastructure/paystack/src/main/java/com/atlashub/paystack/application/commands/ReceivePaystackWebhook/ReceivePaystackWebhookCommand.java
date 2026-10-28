package com.atlashub.paystack.application.commands.ReceivePaystackWebhook;

import com.atlashub.paystack.infrastructure.external.paystack.configuration.PaystackEnvironment;
import java.util.Arrays;

public record ReceivePaystackWebhookCommand(Long providerConnectionId, PaystackEnvironment environment,
                                             byte[] rawBody, String signature) {
    public ReceivePaystackWebhookCommand(PaystackEnvironment environment, byte[] rawBody, String signature) {
        this(null, environment, rawBody, signature);
    }
    public ReceivePaystackWebhookCommand(Long providerConnectionId, byte[] rawBody, String signature) {
        this(providerConnectionId, null, rawBody, signature);
    }
    public ReceivePaystackWebhookCommand { rawBody = rawBody == null ? null : Arrays.copyOf(rawBody, rawBody.length); }
    @Override public byte[] rawBody() { return rawBody == null ? null : Arrays.copyOf(rawBody, rawBody.length); }
}
