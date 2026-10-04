package com.atlashub.iam.infrastructure.messaging.listeners;

import com.atlashub.iam.application.commands.DeactivateMemberByUser.DeactivateMemberByUserCommand;
import com.atlashub.iam.application.commands.DeactivateMemberByUser.DeactivateMemberByUserHandler;
import com.atlashub.iam.infrastructure.messaging.events.EmployeeSuspended;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeoutException;

@Component
public class EmployeeSuspendedListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(EmployeeSuspendedListener.class);
    private static final String GROUP_ID = "iam-employee-group";
    private final DeactivateMemberByUserHandler handler;

    public EmployeeSuspendedListener(ObjectMapper mapper, DeactivateMemberByUserHandler handler) {
        super(mapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() { registerSubscription("EmployeeSuspendedEvent", GROUP_ID); }

    @KafkaListener(topics = "hr-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(payload, "EmployeeSuspendedEvent", EmployeeSuspended.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new DeactivateMemberByUserCommand(
                        event.payload().organizationId(), event.payload().userId())));
    }
}
