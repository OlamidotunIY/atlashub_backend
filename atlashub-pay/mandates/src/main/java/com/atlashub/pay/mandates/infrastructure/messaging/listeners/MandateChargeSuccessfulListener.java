package com.atlashub.pay.mandates.infrastructure.messaging.listeners;

import com.atlashub.pay.mandates.application.commands.AdvanceMandateDate.AdvanceMandateDateCommand;
import com.atlashub.pay.mandates.application.commands.AdvanceMandateDate.AdvanceMandateDateHandler;
import com.atlashub.pay.mandates.infrastructure.messaging.events.ChargeSuccessfulKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class MandateChargeSuccessfulListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(MandateChargeSuccessfulListener.class);
    private static final String GROUP_ID = "pay-mandates-charge-successful";

    private final AdvanceMandateDateHandler advanceMandateDateHandler;

    public MandateChargeSuccessfulListener(ObjectMapper objectMapper, AdvanceMandateDateHandler advanceMandateDateHandler) {
        super(objectMapper);
        this.advanceMandateDateHandler = advanceMandateDateHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("ChargeSuccessfulEvent", GROUP_ID);
    }

    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(
                payload,
                "ChargeSuccessfulEvent",
                ChargeSuccessfulKafkaEvent.class,
                log,
                GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> {
                    if ("MANDATE_DEBIT".equalsIgnoreCase(event.payload().sourceSystem())
                            && event.payload().sourceReferenceId() != null) {
                        try {
                            Long mandateId = Long.parseLong(event.payload().sourceReferenceId());
                            advanceMandateDateHandler.execute(new AdvanceMandateDateCommand(
                                    mandateId,
                                    event.payload().chargeReference()
                            ));
                        } catch (NumberFormatException e) {
                            log.warn("Invalid mandate ID in sourceReferenceId: {}", event.payload().sourceReferenceId());
                        }
                    }
                }
        );
    }
}
