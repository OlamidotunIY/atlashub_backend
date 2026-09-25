package com.atlashub.authentication.infrastructure.messaging.listeners;

import com.atlashub.authentication.application.command.AuthAccount.AuthAccountCommand;
import com.atlashub.authentication.application.command.AuthAccount.AuthAccountHandler;
import com.atlashub.authentication.infrastructure.messaging.events.UserCreated;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class UserAuthenticationListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserAuthenticationListener.class);
    private static final String GROUP_ID = "authentication-group";
    private final AuthAccountHandler authAccountHandler;

    public UserAuthenticationListener(ObjectMapper objectMapper, AuthAccountHandler authAccountHandler) {
        super(objectMapper);
        this.authAccountHandler = authAccountHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("UserCreated", GROUP_ID);
    }

    @KafkaListener(topics = "user-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "UserCreated", UserCreated.class, log, GROUP_ID,
                e -> e instanceof TimeoutException, event -> {
                    AuthAccountCommand command = new AuthAccountCommand(
                            event.aggregateId(), event.payload().email(), event.payload().hashedPassword());
                    authAccountHandler.execute(command);
                });
    }
}
