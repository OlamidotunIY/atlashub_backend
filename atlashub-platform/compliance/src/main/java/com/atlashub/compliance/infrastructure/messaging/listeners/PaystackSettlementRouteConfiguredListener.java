package com.atlashub.compliance.infrastructure.messaging.listeners;

import com.atlashub.compliance.application.commands.CompletePaystackOnboarding.CompletePaystackOnboardingCommand;
import com.atlashub.compliance.application.commands.CompletePaystackOnboarding.CompletePaystackOnboardingHandler;
import com.atlashub.compliance.infrastructure.messaging.events.PaystackSettlementRouteConfiguredEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class PaystackSettlementRouteConfiguredListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(PaystackSettlementRouteConfiguredListener.class);
    private static final String GROUP_ID = "compliance-paystack-route-configured";
    private final CompletePaystackOnboardingHandler handler;

    public PaystackSettlementRouteConfiguredListener(
            ObjectMapper objectMapper,
            CompletePaystackOnboardingHandler handler
    ) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription(
                "com.atlashub.pay.accounts.domain.events.PaystackSettlementRouteConfiguredEvent",
                GROUP_ID
        );
    }

    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, PaystackSettlementRouteConfiguredEvent.class.getSimpleName(),
                PaystackSettlementRouteConfiguredEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new CompletePaystackOnboardingCommand(
                        event.payload().organizationId(), event.payload().environment(), event.payload().provider(),
                        event.payload().capabilities(), event.payload().externalMerchantId(),
                        event.payload().externalAccountId(), event.payload().settlementAccountReference())));
    }
}
