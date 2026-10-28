package com.atlashub.integrations.application.command.CompleteStripeAuthorization;

import com.atlashub.integrations.application.services.ConnectionService;
import com.atlashub.integrations.application.services.StripeAuthorizationService;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class CompleteStripeAuthorizationHandler extends Command<CompleteStripeAuthorizationCommand, ConnectionService.ConnectedAuthorization> {
    private final StripeAuthorizationService stripe;
    public CompleteStripeAuthorizationHandler(StripeAuthorizationService stripe) { this.stripe = stripe; }
    @Override public ConnectionService.ConnectedAuthorization execute(CompleteStripeAuthorizationCommand command) {
        if (command.error() != null) { stripe.denied(command.state()); throw new IllegalArgumentException("Stripe authorization was denied"); }
        return stripe.callback(command.state(), command.code());
    }
}
