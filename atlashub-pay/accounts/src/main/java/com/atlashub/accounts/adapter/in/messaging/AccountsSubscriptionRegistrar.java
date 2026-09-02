package com.atlashub.accounts.adapter.in.messaging;

import com.atlashub.shared.application.api.EventTrackerApi;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AccountsSubscriptionRegistrar implements ApplicationRunner {

    private final EventTrackerApi eventTrackerApi;

    public AccountsSubscriptionRegistrar(EventTrackerApi eventTrackerApi) {
        this.eventTrackerApi = eventTrackerApi;
    }

    @Override
    public void run(ApplicationArguments args) {
        eventTrackerApi.registerSubscription("OrganizationBanned", "accounts-module-group");
        eventTrackerApi.registerSubscription("OrganizationComplianceApproved", "accounts-module-group");
    }
}
