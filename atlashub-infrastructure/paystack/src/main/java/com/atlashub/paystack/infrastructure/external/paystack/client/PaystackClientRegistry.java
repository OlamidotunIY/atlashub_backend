package com.atlashub.paystack.infrastructure.external.paystack.client;

import com.atlashub.paystack.infrastructure.external.paystack.configuration.PaystackEnvironment;

import java.util.Map;

public final class PaystackClientRegistry {
    private final Map<PaystackEnvironment, PaystackClients> clients;

    public PaystackClientRegistry(Map<PaystackEnvironment, PaystackClients> clients) {
        this.clients = Map.copyOf(clients);
    }

    public PaystackClients forEnvironment(PaystackEnvironment environment) {
        PaystackClients result = clients.get(environment);
        if (result == null) throw new IllegalStateException("Paystack clients are unavailable for " + environment);
        return result;
    }
}
