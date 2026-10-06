package com.atlashub.pay.accounts.infrastructure.messaging.listeners;

import com.atlashub.pay.accounts.application.commands.ActivatePaymentProviderProfile.ActivatePaymentProviderProfileCommand;
import com.atlashub.pay.accounts.application.commands.ActivatePaymentProviderProfile.ActivatePaymentProviderProfileHandler;
import com.atlashub.pay.accounts.infrastructure.messaging.events.ProviderOnboardingApprovedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class ProviderOnboardingApprovedListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(ProviderOnboardingApprovedListener.class);
    private static final String GROUP_ID = "pay-accounts-provider-onboarding-approved";
    private final ActivatePaymentProviderProfileHandler handler;

    public ProviderOnboardingApprovedListener(
            ObjectMapper objectMapper,
            ActivatePaymentProviderProfileHandler handler
    ) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("com.atlashub.compliance.domain.events.ProviderOnboardingApprovedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "compliance-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, ProviderOnboardingApprovedEvent.class.getSimpleName(),
                ProviderOnboardingApprovedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new ActivatePaymentProviderProfileCommand(
                        event.payload().onboardingCaseId(), event.payload().organizationId(),
                        event.payload().environment(), event.payload().provider(),
                        event.payload().capabilities(), event.payload().externalMerchantId(),
                        event.payload().externalAccountId(),
                        event.payload().settlementAccountReference())));
    }
}
