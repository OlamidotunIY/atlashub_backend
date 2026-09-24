package com.atlashub.audit.adapter.in.messaging;

import com.atlashub.audit.application.usecase.LogActivityUseCase;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.atlashub.shared.application.port.EventTrackerPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class AuthAuditEventListener extends BaseKafkaEventListener {

    private final LogActivityUseCase logActivityUseCase;

    public AuthAuditEventListener(LogActivityUseCase logActivityUseCase, ObjectMapper objectMapper, EventTrackerPort EventTrackerPort) {
        super(objectMapper);
        this.logActivityUseCase = logActivityUseCase;
    }
}
