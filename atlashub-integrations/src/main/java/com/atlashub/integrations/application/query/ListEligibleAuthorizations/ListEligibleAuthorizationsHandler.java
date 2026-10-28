package com.atlashub.integrations.application.query.ListEligibleAuthorizations;

import com.atlashub.integrations.application.services.ConnectionService;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ListEligibleAuthorizationsHandler extends Query<ListEligibleAuthorizationsQuery, List<ConnectionService.ConnectedAuthorization>> {
    private final ConnectionService connections;
    public ListEligibleAuthorizationsHandler(ConnectionService connections) { this.connections = connections; }
    @Override public List<ConnectionService.ConnectedAuthorization> execute(ListEligibleAuthorizationsQuery query) {
        return connections.eligible(query.organizationId(), query.environment(), query.currency(), query.capability());
    }
}
