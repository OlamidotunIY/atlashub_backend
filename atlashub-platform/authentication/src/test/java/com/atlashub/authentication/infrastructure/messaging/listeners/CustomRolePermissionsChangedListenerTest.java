package com.atlashub.authentication.infrastructure.messaging.listeners;

import com.atlashub.authentication.application.command.RevokeOrganizationSessions.RevokeOrganizationSessionsCommand;
import com.atlashub.authentication.application.command.RevokeOrganizationSessions.RevokeOrganizationSessionsHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CustomRolePermissionsChangedListenerTest {
    @Test
    void delegates_role_permission_change_to_session_revocation_command() {
        RevokeOrganizationSessionsHandler handler = mock(RevokeOrganizationSessionsHandler.class);
        var listener = new CustomRolePermissionsChangedListener(new ObjectMapper(), handler);
        listener.listen("""
                {"eventType":"CustomRolePermissionsChangedEvent","event":{
                  "aggregateId":7,"payload":{"organizationId":42}
                }}
                """);
        verify(handler).execute(new RevokeOrganizationSessionsCommand(42L));
    }
}
