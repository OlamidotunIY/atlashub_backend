package com.atlashub.integrations.application.command.DisconnectProvider;

import com.atlashub.integrations.application.services.ConnectionService;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class DisconnectProviderHandler extends Command<DisconnectProviderCommand, Void> {
    private final ConnectionService connections;
    public DisconnectProviderHandler(ConnectionService connections) { this.connections = connections; }
    @Override public Void execute(DisconnectProviderCommand command) {
        connections.disconnect(command.organizationId(), command.connectionId()); return null;
    }
}
