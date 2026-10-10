package com.atlashub.paystack.infrastructure.external.paystack.messaging;

import com.atlashub.paystack.infrastructure.external.paystack.configuration.PaystackProperties;
import com.atlashub.paystack.infrastructure.external.paystack.messaging.events.PaystackEventEnvelope;
import com.atlashub.paystack.infrastructure.external.paystack.messaging.events.PaystackWebhookReceivedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix="atlashub.integrations.paystack", name="enabled", havingValue="true")
public class PaystackWebhookEventPublisher {
    public static final String TOPIC = "paystack-events";
    private final KafkaTemplate<String,String> kafka;
    private final ObjectMapper mapper;
    private final PaystackProperties properties;
    public PaystackWebhookEventPublisher(KafkaTemplate<String,String> kafka, ObjectMapper mapper,
                                          PaystackProperties properties) {
        this.kafka=kafka; this.mapper=mapper; this.properties=properties;
    }
    public void publish(PaystackWebhookReceivedEvent event) {
        try {
            String body=mapper.writeValueAsString(new PaystackEventEnvelope(
                    PaystackWebhookReceivedEvent.class.getSimpleName(), event.eventId(), event));
            kafka.send(TOPIC,event.eventId(),body).get(properties.webhookPublishTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Paystack webhook publishing was interrupted", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Paystack webhook could not be published", ex);
        }
    }
}
