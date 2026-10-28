package com.atlashub.integrations.application.command.VerifyProviderAuthorization;

import com.atlashub.integrations.application.services.ConnectionService;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class VerifyProviderAuthorizationHandler extends Command<VerifyProviderAuthorizationCommand, ConnectionService.ConnectedAuthorization> {
    private final ConnectionService connections;
    public VerifyProviderAuthorizationHandler(ConnectionService connections) { this.connections = connections; }
    @Override public ConnectionService.ConnectedAuthorization execute(VerifyProviderAuthorizationCommand command) {
        return connections.verify(command.organizationId(), command.authorizationId());
    }
}
