package com.atlashub.pay.mandates.infrastructure.messaging.listeners;

import com.atlashub.pay.mandates.application.commands.RevokeEmployeeMandates.RevokeEmployeeMandatesCommand;
import com.atlashub.pay.mandates.application.commands.RevokeEmployeeMandates.RevokeEmployeeMandatesHandler;
import com.atlashub.pay.mandates.infrastructure.messaging.events.EmployeeTerminatedKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class EmployeeTerminatedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(EmployeeTerminatedListener.class);
    private static final String GROUP_ID = "pay-mandates-employee-terminated";

    private final RevokeEmployeeMandatesHandler revokeEmployeeMandatesHandler;

    public EmployeeTerminatedListener(ObjectMapper objectMapper, RevokeEmployeeMandatesHandler revokeEmployeeMandatesHandler) {
        super(objectMapper);
        this.revokeEmployeeMandatesHandler = revokeEmployeeMandatesHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("EmployeeTerminatedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "hr-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(
                payload,
                "EmployeeTerminatedEvent",
                EmployeeTerminatedKafkaEvent.class,
                log,
                GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> {
                    if (event.payload().userId() != null && event.payload().organizationId() != null) {
                        revokeEmployeeMandatesHandler.execute(new RevokeEmployeeMandatesCommand(
                                event.payload().organizationId(),
                                event.payload().userId()
                        ));
                    }
                }
        );
    }
}
