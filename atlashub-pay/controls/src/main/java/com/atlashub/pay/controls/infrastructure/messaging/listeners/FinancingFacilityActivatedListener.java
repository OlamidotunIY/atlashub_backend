package com.atlashub.pay.controls.infrastructure.messaging.listeners;

import com.atlashub.pay.controls.application.commands.ActivateControlledFundsPolicy.ActivateControlledFundsPolicyCommand;
import com.atlashub.pay.controls.application.commands.ActivateControlledFundsPolicy.ActivateControlledFundsPolicyHandler;
import com.atlashub.pay.controls.domain.valueobject.ControlMode;
import com.atlashub.pay.controls.domain.valueobject.Environment;
import com.atlashub.pay.controls.infrastructure.messaging.events.FinancingFacilityActivatedKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

@Component
public class FinancingFacilityActivatedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(FinancingFacilityActivatedListener.class);
    private static final String GROUP_ID = "pay-controls-facility-activated";

    private final ActivateControlledFundsPolicyHandler activatePolicyHandler;

    public FinancingFacilityActivatedListener(ObjectMapper objectMapper,
                                             ActivateControlledFundsPolicyHandler activatePolicyHandler) {
        super(objectMapper);
        this.activatePolicyHandler = activatePolicyHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("FinancingFacilityActivatedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "financing-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(
                payload,
                "FinancingFacilityActivatedEvent",
                FinancingFacilityActivatedKafkaEvent.class,
                log,
                GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> {
                    FinancingFacilityActivatedKafkaEvent.Payload p = event.payload();
                    if (p.facilityId() != null && p.organizationId() != null) {
                        ControlMode mode = p.mode() != null ? ControlMode.valueOf(p.mode().toUpperCase()) : ControlMode.PURPOSE_RESTRICTED;
                        activatePolicyHandler.execute(new ActivateControlledFundsPolicyCommand(
                                p.facilityId(),
                                p.organizationId(),
                                Environment.LIVE,
                                p.policyVersion() != null ? p.policyVersion() : "v1.0",
                                mode,
                                Collections.emptySet(),
                                Collections.emptySet(),
                                null,
                                null,
                                null,
                                Collections.emptySet(),
                                ZonedDateTime.now(),
                                null,
                                p.activatedBy() != null ? p.activatedBy() : "FINANCING_SERVICE"
                        ));
                    }
                }
        );
    }
}
