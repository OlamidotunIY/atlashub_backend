package com.atlashub.compliance.infrastructure.messaging.listeners;

import com.atlashub.compliance.application.commands.RequestProviderOnboarding.RequestProviderOnboardingCommand;
import com.atlashub.compliance.application.commands.RequestProviderOnboarding.RequestProviderOnboardingHandler;
import com.atlashub.compliance.infrastructure.messaging.events.ProviderOnboardingRequestedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class ProviderOnboardingRequestedListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(ProviderOnboardingRequestedListener.class);
    private static final String GROUP_ID = "compliance-provider-onboarding-requested";
    private final RequestProviderOnboardingHandler handler;

    public ProviderOnboardingRequestedListener(ObjectMapper objectMapper, RequestProviderOnboardingHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("com.atlashub.pay.accounts.domain.events.ProviderOnboardingRequestedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, "ProviderOnboardingRequestedEvent",
                ProviderOnboardingRequestedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new RequestProviderOnboardingCommand(
                        event.payload().organizationId(), event.payload().environment(),
                        event.payload().provider(), event.payload().capabilities())));
    }
}
