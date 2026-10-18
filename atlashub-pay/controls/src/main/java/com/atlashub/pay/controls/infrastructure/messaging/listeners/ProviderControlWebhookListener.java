package com.atlashub.pay.controls.infrastructure.messaging.listeners;

import com.atlashub.pay.controls.application.commands.ApplyPaymentControlDecision.ApplyPaymentControlDecisionCommand;
import com.atlashub.pay.controls.application.commands.ApplyPaymentControlDecision.ApplyPaymentControlDecisionHandler;
import com.atlashub.pay.controls.infrastructure.messaging.events.ProviderControlCallbackKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class ProviderControlWebhookListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(ProviderControlWebhookListener.class);
    private static final String GROUP_ID = "pay-controls-provider-webhook";

    private final ApplyPaymentControlDecisionHandler applyDecisionHandler;

    public ProviderControlWebhookListener(ObjectMapper objectMapper,
                                          ApplyPaymentControlDecisionHandler applyDecisionHandler) {
        super(objectMapper);
        this.applyDecisionHandler = applyDecisionHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("ProviderControlCallbackEvent", GROUP_ID);
    }

    @KafkaListener(topics = "provider-callbacks", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(
                payload,
                "ProviderControlCallbackEvent",
                ProviderControlCallbackKafkaEvent.class,
                log,
                GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> {
                    ProviderControlCallbackKafkaEvent.Payload p = event.payload();
                    if (p.decisionId() != null) {
                        applyDecisionHandler.execute(new ApplyPaymentControlDecisionCommand(
                                p.decisionId(),
                                p.providerReference()
                        ));
                    }
                }
        );
    }
}
