package com.atlashub.identity.adapter.in.messaging;

import com.atlashub.shared.application.port.out.EventTrackerPort;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class IdentitySubscriptionRegistrar implements ApplicationRunner {

    private final EventTrackerPort EventTrackerPort;

    public IdentitySubscriptionRegistrar(EventTrackerPort EventTrackerPort) {
        this.EventTrackerPort = EventTrackerPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        EventTrackerPort.registerSubscription("OrganizationRegistered", "identity-module-group");
    }
}
