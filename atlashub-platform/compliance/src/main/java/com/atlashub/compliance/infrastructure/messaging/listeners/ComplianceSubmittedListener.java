package com.atlashub.compliance.infrastructure.messaging.listeners;

import com.atlashub.compliance.application.commands.OrchestrateAnchorCompliance.*;
import com.atlashub.compliance.domain.events.ComplianceSubmittedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.concurrent.TimeoutException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpClientErrorException;

@Component
@ConditionalOnProperty(prefix = "atlashub.integrations.anchor", name = "enabled", havingValue = "true")
public class ComplianceSubmittedListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(ComplianceSubmittedListener.class);
    private static final String GROUP_ID = "compliance-anchor-submission";
    private final OrchestrateAnchorComplianceHandler handler;
    public ComplianceSubmittedListener(ObjectMapper mapper, OrchestrateAnchorComplianceHandler handler) {
        super(mapper); this.handler = handler;
    }
    @PostConstruct public void init() { registerSubscription(ComplianceSubmittedEvent.class.getName(), GROUP_ID); }
    @KafkaListener(topics = "compliance-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, ComplianceSubmittedEvent.class.getSimpleName(), ComplianceSubmittedEvent.class,
                log, GROUP_ID, this::retryable,
                event -> handler.execute(new OrchestrateAnchorComplianceCommand(event.payload().organizationId())));
    }
    private boolean retryable(Exception error) {
        Throwable cause = error;
        while (cause != null) {
            if (cause instanceof TimeoutException || cause instanceof ResourceAccessException
                    || cause instanceof HttpServerErrorException
                    || cause instanceof HttpClientErrorException.TooManyRequests) return true;
            cause = cause.getCause();
        }
        return false;
    }
}
