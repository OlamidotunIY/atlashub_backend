package com.atlashub.auth.adapter.in.messaging;

import com.atlashub.shared.application.port.out.EventTrackerPort;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AuthSubscriptionRegistrar implements ApplicationRunner {

    private final EventTrackerPort EventTrackerPort;

    public AuthSubscriptionRegistrar(EventTrackerPort EventTrackerPort) {
        this.EventTrackerPort = EventTrackerPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        EventTrackerPort.registerSubscription("AdminCreatedEvent", "auth-module-admin-group");
        EventTrackerPort.registerSubscription("UserCreated", "auth-module-group");
    }
}
