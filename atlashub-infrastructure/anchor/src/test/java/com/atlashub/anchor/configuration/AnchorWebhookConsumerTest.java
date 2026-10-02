package com.atlashub.anchor.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnchorWebhookConsumerTest {

    @Test
    void defines_a_strict_atlas_hub_callback_contract_per_consumer() {
        assertEquals(AnchorWebhookConsumer.PAY_ACCOUNTS,
                AnchorWebhookConsumer.fromCallbackPath("pay-accounts"));
        assertTrue(AnchorWebhookConsumer.COMPLIANCE.enabledEvents()
                .contains("customer.identification.approved"));
        assertTrue(AnchorWebhookConsumer.PAY_ACCOUNTS.enabledEvents().contains("payin.received"));
    }
}
