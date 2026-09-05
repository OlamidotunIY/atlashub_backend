package com.atlashub.accounts.adapter.in.messaging;

import com.atlashub.shared.application.port.out.EventTrackerPort;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AccountsSubscriptionRegistrar implements ApplicationRunner {

    private final EventTrackerPort EventTrackerPort;

    public AccountsSubscriptionRegistrar(EventTrackerPort EventTrackerPort) {
        this.EventTrackerPort = EventTrackerPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        EventTrackerPort.registerSubscription("OrganizationBanned", "accounts-module-group");
        EventTrackerPort.registerSubscription("OrganizationComplianceApproved", "accounts-module-group");
        EventTrackerPort.registerSubscription("OrganizationRegistered", "accounts-module-bootstrap-group");
    }
}
