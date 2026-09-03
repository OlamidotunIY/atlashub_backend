package com.atlashub.charges.adapter.in.web.request;

import java.util.Map;

public record PaystackWebhookRequest(
        String event,
        Map<String, Object> data
) {}
