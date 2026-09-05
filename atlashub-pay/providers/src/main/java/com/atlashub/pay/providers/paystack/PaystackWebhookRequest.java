package com.atlashub.pay.providers.paystack;

import java.util.Map;

public record PaystackWebhookRequest(
        String event,
        Map<String, Object> data
) {}
