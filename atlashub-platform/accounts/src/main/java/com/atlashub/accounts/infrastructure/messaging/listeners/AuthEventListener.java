package com.atlashub.accounts.infrastructure.messaging.listeners;

import com.atlashub.accounts.domain.repository.UserRepository;
import com.atlashub.accounts.infrastructure.messaging.events.AuthEmailVerified;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class AuthEventListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthEventListener.class);
    private static final String GROUP_ID = "accounts-auth-group";

    private final UserRepository userRepository;

    public AuthEventListener(ObjectMapper objectMapper, UserRepository userRepository) {
        super(objectMapper);
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        registerSubscription("AuthEmailVerifiedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "auth-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "AuthEmailVerifiedEvent", AuthEmailVerified.class, log, GROUP_ID,
                e -> e instanceof TimeoutException, event -> {
                    Long userId = Long.parseLong(event.payload().userId());
                    userRepository.findById(userId).ifPresent(user -> {
                        user.markEmailVerified();
                        userRepository.save(user);
                        log.info("Email verified for userId={}", userId);
                    });
                });
    }
}
