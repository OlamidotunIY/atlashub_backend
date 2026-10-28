package com.atlashub.integrations.application.query.ListProviderConnections;

import com.atlashub.integrations.application.services.ConnectionService;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ListProviderConnectionsHandler extends Query<ListProviderConnectionsQuery, List<ConnectionService.ConnectedAuthorization>> {
    private final ConnectionService connections;
    public ListProviderConnectionsHandler(ConnectionService connections) { this.connections = connections; }
    @Override public List<ConnectionService.ConnectedAuthorization> execute(ListProviderConnectionsQuery query) {
        return connections.list(query.organizationId());
    }
}
