package com.atlashub.anchor.configuration;

import com.atlashub.anchor.infrastructure.messaging.AnchorWebhookEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;

/** Kafka infrastructure for verified inbound Anchor provider events. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "atlashub.integrations.anchor", name = "enabled", havingValue = "true")
public class AnchorMessagingConfiguration {
    private static final String RETENTION_MS = "86400000";

    @Bean
    NewTopic anchorEventsTopic() {
        return TopicBuilder.name(AnchorWebhookEventPublisher.TOPIC)
                .config("retention.ms", RETENTION_MS)
                .build();
    }
}
