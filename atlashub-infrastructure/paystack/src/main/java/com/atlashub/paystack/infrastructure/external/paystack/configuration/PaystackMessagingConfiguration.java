package com.atlashub.paystack.infrastructure.external.paystack.configuration;

import com.atlashub.paystack.infrastructure.external.paystack.messaging.PaystackWebhookEventPublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods=false)
@ConditionalOnProperty(prefix="atlashub.integrations.paystack", name="enabled", havingValue="true")
public class PaystackMessagingConfiguration {
    @Bean NewTopic paystackEventsTopic() {
        return TopicBuilder.name(PaystackWebhookEventPublisher.TOPIC).config("retention.ms", "86400000").build();
    }
}
