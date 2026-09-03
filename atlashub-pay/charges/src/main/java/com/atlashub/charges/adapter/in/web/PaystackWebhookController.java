package com.atlashub.charges.adapter.in.web;

import com.atlashub.charges.domain.event.PaymentSuccessfulEvent;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks/paystack")
public class PaystackWebhookController {

    private final DomainEventPublisher publisher;

    public PaystackWebhookController(DomainEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, Object> payload) {
        String event = (String) payload.get("event");
        if ("charge.success".equals(event)) {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String reference = (String) data.get("reference");
            
            // In a real app, purpose and metadata would be extracted from the custom_fields or metadata of the Paystack payload
            String purpose = "PLATFORM_INVOICE"; // Simplified for now
            String metadata = "123"; // Simplified

            // Alternatively we expect it in data.metadata.purpose
            if (data.containsKey("metadata")) {
                Map<String, Object> metaMap = (Map<String, Object>) data.get("metadata");
                if (metaMap.containsKey("purpose")) {
                    purpose = (String) metaMap.get("purpose");
                }
                if (metaMap.containsKey("invoiceId")) {
                    metadata = (String) metaMap.get("invoiceId");
                }
            }

            PaymentSuccessfulEvent successEvent = new PaymentSuccessfulEvent(
                    UUID.randomUUID().toString(),
                    reference,
                    ZonedDateTime.now(),
                    new PaymentSuccessfulEvent.Payload(reference, purpose, metadata)
            );
            
            publisher.publish(EnvelopedDomainEvent.wrap(successEvent));
        }

        return ResponseEntity.ok().build();
    }
}