package com.atlashub.compliance.application.commands.ApplyAnchorWebhook;
public record ApplyAnchorWebhookCommand(String eventType, String customerId, String documentId,
        String reason, String failureCode) {}
