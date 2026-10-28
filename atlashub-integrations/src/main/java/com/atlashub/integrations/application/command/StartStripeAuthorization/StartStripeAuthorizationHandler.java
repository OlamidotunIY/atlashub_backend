package com.atlashub.integrations.application.command.StartStripeAuthorization;

import com.atlashub.integrations.application.services.StripeAuthorizationService;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class StartStripeAuthorizationHandler extends Command<StartStripeAuthorizationCommand, StripeAuthorizationService.StartResult> {
    private final StripeAuthorizationService stripe;
    public StartStripeAuthorizationHandler(StripeAuthorizationService stripe) { this.stripe = stripe; }
    @Override public StripeAuthorizationService.StartResult execute(StartStripeAuthorizationCommand command) {
        return stripe.start(command.organizationId(), command.environment(), command.merchantCountry());
    }
}
