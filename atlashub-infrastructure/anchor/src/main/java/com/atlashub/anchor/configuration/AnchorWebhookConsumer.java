package com.atlashub.anchor.configuration;

import java.util.List;

/** AtlasHub module callback subscriptions registered with Anchor for each environment. */
public enum AnchorWebhookConsumer {
    COMPLIANCE(
            "compliance",
            "atlashub-compliance",
            List.of(
                    "customer.identification.approved", "customer.identification.error",
                    "customer.identification.rejected",
                    "customer.identification.awaitingDocument", "document.approved", "document.rejected"
            )
    ),
    PAY_ACCOUNTS(
            "pay-accounts",
            "atlashub-pay-accounts",
            List.of(
                    "account.opened", "account.closed", "account.frozen", "account.unfrozen",
                    "account.creation.failed", "sub_account.created", "accountNumber.created",
                    "payin.received", "payment.received", "payment.settled"
            )
    );

    private final String callbackSegment;
    private final String label;
    private final List<String> enabledEvents;

    AnchorWebhookConsumer(String callbackSegment, String label, List<String> enabledEvents) {
        this.callbackSegment = callbackSegment;
        this.label = label;
        this.enabledEvents = List.copyOf(enabledEvents);
    }

    public String callbackSegment() {
        return callbackSegment;
    }

    public String label() {
        return label;
    }

    public List<String> enabledEvents() {
        return enabledEvents;
    }

    public static AnchorWebhookConsumer fromCallbackPath(String value) {
        for (AnchorWebhookConsumer consumer : values()) {
            if (consumer.callbackSegment.equals(value)) {
                return consumer;
            }
        }
        throw new IllegalArgumentException("Unsupported Anchor webhook consumer");
    }
}
