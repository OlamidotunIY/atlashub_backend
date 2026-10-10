package com.atlashub.anchor.infrastructure.external.anchor.webhook;

import com.atlashub.anchor.infrastructure.external.anchor.client.AnchorClientRegistry;
import com.atlashub.anchor.infrastructure.external.anchor.configuration.AnchorEnvironment;
import com.atlashub.anchor.infrastructure.external.anchor.configuration.AnchorProperties;
import com.atlashub.anchor.infrastructure.external.anchor.configuration.AnchorWebhookConsumer;
import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorRequest;
import com.atlashub.anchor.infrastructure.external.anchor.dto.webhook.AnchorWebhookResource;
import com.atlashub.anchor.infrastructure.external.anchor.dto.webhook.CreateAnchorWebhookData;
import com.atlashub.anchor.exception.AnchorWebhookSubscriptionConflictException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Explicitly creates a module's Anchor webhook only when its exact desired subscription is absent.
 * It is intentionally not a startup task; operations invokes it after public URLs and tokens exist.
 */
@Component
@ConditionalOnProperty(prefix = "atlashub.integrations.anchor", name = "enabled", havingValue = "true")
public class AnchorWebhookSubscriptionProvisioner {
    private static final String DELIVERY_MODE = "AtLeastOnce";

    private final AnchorClientRegistry clientRegistry;
    private final AnchorProperties properties;

    public AnchorWebhookSubscriptionProvisioner(AnchorClientRegistry clientRegistry, AnchorProperties properties) {
        this.clientRegistry = clientRegistry;
        this.properties = properties;
    }

    public AnchorWebhookResource ensureSubscription(AnchorEnvironment environment, AnchorWebhookConsumer consumer) {
        AnchorProperties.WebhookSubscriptionProperties subscription = properties.forEnvironment(environment)
                .webhooks()
                .forConsumer(consumer);
        List<AnchorWebhookResource> existingWebhooks = clientRegistry.forEnvironment(environment)
                .webhooks()
                .listWebhooks()
                .data();

        for (AnchorWebhookResource webhook : existingWebhooks == null ? List.<AnchorWebhookResource>of() : existingWebhooks) {
            if (webhook.attributes() != null && consumer.label().equals(webhook.attributes().label())) {
                if (matchesDesiredContract(webhook, subscription, consumer)) {
                    return webhook;
                }
                throw new AnchorWebhookSubscriptionConflictException(
                        "Anchor webhook label already exists with a different callback contract: " + consumer.label()
                );
            }
        }

        return clientRegistry.forEnvironment(environment).webhooks().createWebhook(new AnchorRequest<>(
                new CreateAnchorWebhookData(new CreateAnchorWebhookData.Attributes(
                        DELIVERY_MODE,
                        subscription.callbackUrl(),
                        subscription.token(),
                        consumer.label(),
                        true,
                        consumer.enabledEvents()
                ))
        )).data();
    }

    private boolean matchesDesiredContract(
            AnchorWebhookResource webhook,
            AnchorProperties.WebhookSubscriptionProperties subscription,
            AnchorWebhookConsumer consumer
    ) {
        AnchorWebhookResource.Attributes attributes = webhook.attributes();
        return DELIVERY_MODE.equals(attributes.deliveryMode())
                && subscription.callbackUrl().toString().equals(attributes.url())
                && Boolean.TRUE.equals(attributes.supportIncluded())
                && attributes.enabledEvents() != null
                && attributes.enabledEvents().containsAll(consumer.enabledEvents())
                && consumer.enabledEvents().containsAll(attributes.enabledEvents());
    }
}
