package com.atlashub.pay.accounts.infrastructure.messaging.listeners;

import com.atlashub.pay.accounts.application.commands.ApplyPaymentProviderOnboardingStatus.ApplyPaymentProviderOnboardingStatusCommand;
import com.atlashub.pay.accounts.application.commands.ApplyPaymentProviderOnboardingStatus.ApplyPaymentProviderOnboardingStatusHandler;
import com.atlashub.pay.accounts.infrastructure.messaging.events.ProviderOnboardingStatusChangedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class ProviderOnboardingStatusChangedListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(ProviderOnboardingStatusChangedListener.class);
    private static final String GROUP_ID = "pay-accounts-provider-onboarding-status";
    private final ApplyPaymentProviderOnboardingStatusHandler handler;

    public ProviderOnboardingStatusChangedListener(
            ObjectMapper objectMapper,
            ApplyPaymentProviderOnboardingStatusHandler handler
    ) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription(ProviderOnboardingStatusChangedEvent.class.getName(), GROUP_ID);
    }

    @KafkaListener(topics = "compliance-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, ProviderOnboardingStatusChangedEvent.class.getSimpleName(),
                ProviderOnboardingStatusChangedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new ApplyPaymentProviderOnboardingStatusCommand(
                        event.payload().onboardingCaseId(), event.payload().organizationId(),
                        event.payload().environment(), event.payload().provider(), event.payload().status(),
                        event.payload().failureCode(), event.payload().failureMessage())));
    }
}
