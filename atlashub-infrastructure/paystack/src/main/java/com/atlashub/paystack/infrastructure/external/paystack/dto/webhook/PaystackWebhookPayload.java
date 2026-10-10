package com.atlashub.paystack.infrastructure.external.paystack.dto.webhook;

import com.fasterxml.jackson.databind.JsonNode;

public record PaystackWebhookPayload(String event, JsonNode data) {}
