package com.atlashub.compliance.infrastructure.messaging.listeners;

import com.atlashub.compliance.application.commands.RecordAnchorDecision.RecordAnchorDecisionCommand;
import com.atlashub.compliance.application.commands.RecordAnchorDecision.RecordAnchorDecisionHandler;
import com.atlashub.compliance.infrastructure.messaging.events.KycApprovedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class KycApprovedListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(KycApprovedListener.class);
    private static final String GROUP_ID = "compliance-anchor-kyb-approved";
    private final RecordAnchorDecisionHandler handler;

    public KycApprovedListener(ObjectMapper objectMapper, RecordAnchorDecisionHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() { registerSubscription("KycApprovedEvent", GROUP_ID); }

    @KafkaListener(topics = "anchor-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(payload, "KycApprovedEvent", KycApprovedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException, event -> {
                    handler.execute(new RecordAnchorDecisionCommand(event.payload().organizationId(),
                            event.payload().anchorBusinessCustomerId(), true, null));
                });
    }
}
