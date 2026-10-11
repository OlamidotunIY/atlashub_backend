package com.atlashub.pay.settlement.infrastructure.messaging.events;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrganizationAccountFundedEventTest {

    @Test
    void deserializesExtendedAnchorFundingEvidence() throws Exception {
        var mapper = JsonMapper.builder().findAndAddModules().build();
        String json = """
                {
                  "eventId": "event-1",
                  "aggregateId": 91,
                  "occurredAt": "2026-10-11T10:15:30Z",
                  "correlationId": "correlation-1",
                  "payload": {
                    "organizationId": 12,
                    "environment": "LIVE",
                    "businessAccountId": 91,
                    "anchorTransferReference": "anchor-credit-1",
                    "amount": 2500.00,
                    "currency": "NGN",
                    "senderAccountName": "Paystack Settlement",
                    "senderBankCode": "999999",
                    "narration": "Settlement payout",
                    "receivedAt": "2026-10-11T10:15:00Z"
                  }
                }
                """;

        OrganizationAccountFundedEvent event = mapper.readValue(json, OrganizationAccountFundedEvent.class);

        assertEquals("anchor-credit-1", event.payload().anchorTransferReference());
        assertEquals("Paystack Settlement", event.payload().senderAccountName());
        assertEquals("999999", event.payload().senderBankCode());
        assertEquals("Settlement payout", event.payload().narration());
    }
}
