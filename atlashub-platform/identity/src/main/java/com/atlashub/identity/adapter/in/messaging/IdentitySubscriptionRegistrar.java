package com.atlashub.identity.adapter.in.messaging;

import com.atlashub.shared.api.EventTrackerApi;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class IdentitySubscriptionRegistrar implements ApplicationRunner {

    private final EventTrackerApi eventTrackerApi;

    public IdentitySubscriptionRegistrar(EventTrackerApi eventTrackerApi) {
        this.eventTrackerApi = eventTrackerApi;
    }

    @Override
    public void run(ApplicationArguments args) {
        eventTrackerApi.registerSubscription("OrganizationRegistered", "identity-module-group");
    }
}
