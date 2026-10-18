package com.atlashub.pay.controls.infrastructure.messaging.listeners;

import com.atlashub.pay.controls.application.commands.EscalateFinancingAnomaly.EscalateFinancingAnomalyCommand;
import com.atlashub.pay.controls.application.commands.EscalateFinancingAnomaly.EscalateFinancingAnomalyHandler;
import com.atlashub.pay.controls.infrastructure.messaging.events.FinancingAnomalyKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class FinancingAnomalyListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(FinancingAnomalyListener.class);
    private static final String GROUP_ID = "pay-controls-financing-anomaly";

    private final EscalateFinancingAnomalyHandler escalateAnomalyHandler;

    public FinancingAnomalyListener(ObjectMapper objectMapper,
                                    EscalateFinancingAnomalyHandler escalateAnomalyHandler) {
        super(objectMapper);
        this.escalateAnomalyHandler = escalateAnomalyHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("FinancingAnomalyDetectedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "financing-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(
                payload,
                "FinancingAnomalyDetectedEvent",
                FinancingAnomalyKafkaEvent.class,
                log,
                GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> {
                    FinancingAnomalyKafkaEvent.Payload p = event.payload();
                    if (p.facilityId() != null && p.organizationId() != null) {
                        escalateAnomalyHandler.execute(new EscalateFinancingAnomalyCommand(
                                p.facilityId(),
                                p.organizationId(),
                                p.anomalyType(),
                                p.severity(),
                                p.riskSignalId()
                        ));
                    }
                }
        );
    }
}
