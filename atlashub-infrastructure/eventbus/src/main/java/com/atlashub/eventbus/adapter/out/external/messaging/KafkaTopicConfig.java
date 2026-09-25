package com.atlashub.eventbus.adapter.out.external.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    private static final String RETENTION_MS = "86400000"; // 24 hours

    @Bean
    public NewTopic adminEventsTopic() {
        return TopicBuilder.name("admin-events")
                .config("retention.ms", RETENTION_MS)
                .build();
    }

    @Bean
    public NewTopic authEventsTopic() {
        return TopicBuilder.name("auth-events")
                .config("retention.ms", RETENTION_MS)
                .build();
    }

    @Bean
    public NewTopic identityEventsTopic() {
        return TopicBuilder.name("identity-events")
                .config("retention.ms", RETENTION_MS)
                .build();
    }
    
    @Bean
    public NewTopic accountEventsTopic() {
        return TopicBuilder.name("account-events")
                .config("retention.ms", RETENTION_MS)
                .build();
    }

    @Bean
    public NewTopic webhookEventsTopic() {
        return TopicBuilder.name("WebhookDeliveryRequested")
                .config("retention.ms", RETENTION_MS)
                .build();
    }

    @Bean
    public NewTopic payEventsTopic() {
        return TopicBuilder.name("pay-events")
                .config("retention.ms", RETENTION_MS)
                .build();
    }

    @Bean
    public NewTopic organizationMemberEventsTopic() {
        return TopicBuilder.name("organization-member-events")
                .config("retention.ms", RETENTION_MS)
                .build();
    }

    @Bean
    public NewTopic invitationEventsTopic() {
        return TopicBuilder.name("invitation-events")
                .config("retention.ms", RETENTION_MS)
                .build();
    }

    @Bean
    public NewTopic catalogEventsTopic() {
        return TopicBuilder.name("catalog-events")
                .config("retention.ms", RETENTION_MS)
                .build();
    }

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name("user-events")
                .config("retention.ms", RETENTION_MS)
                .build();
    }

    @Bean
    public NewTopic billingEventsTopic() {
        return TopicBuilder.name("billing-events")
                .config("retention.ms", RETENTION_MS)
                .build();
    }

    @Bean
    public NewTopic organizationEventsTopic() {
        return TopicBuilder.name("organization-events")
                .config("retention.ms", RETENTION_MS)
                .build();
    }
}
