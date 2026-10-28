package com.atlashub.integrations.application.command.ConnectProvider;

import com.atlashub.integrations.application.services.ConnectionService;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class ConnectProviderHandler extends Command<ConnectProviderCommand, ConnectionService.ConnectedAuthorization> {
    private final ConnectionService connections;

    public ConnectProviderHandler(ConnectionService connections) {
        this.connections = connections;
    }

    @Override
    public ConnectionService.ConnectedAuthorization execute(ConnectProviderCommand command) {
        return connections.connectCredential(command.organizationId(), command.provider(), command.merchantCountry(),
                command.credential());
    }
}
