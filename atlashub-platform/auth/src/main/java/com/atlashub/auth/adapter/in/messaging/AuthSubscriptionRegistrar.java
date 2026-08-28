package com.atlashub.auth.adapter.in.messaging;

import com.atlashub.shared.api.EventTrackerApi;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AuthSubscriptionRegistrar implements ApplicationRunner {

    private final EventTrackerApi eventTrackerApi;

    public AuthSubscriptionRegistrar(EventTrackerApi eventTrackerApi) {
        this.eventTrackerApi = eventTrackerApi;
    }

    @Override
    public void run(ApplicationArguments args) {
        eventTrackerApi.registerSubscription("AdminCreatedEvent", "auth-module-admin-group");
        eventTrackerApi.registerSubscription("UserCreated", "auth-module-group");
    }
}
