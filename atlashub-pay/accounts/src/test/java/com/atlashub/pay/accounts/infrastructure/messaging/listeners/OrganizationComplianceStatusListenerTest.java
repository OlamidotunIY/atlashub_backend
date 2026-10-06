package com.atlashub.pay.accounts.infrastructure.messaging.listeners;

import com.atlashub.pay.accounts.application.commands.ApplyOrganizationBankingRestriction.ApplyOrganizationBankingRestrictionCommand;
import com.atlashub.pay.accounts.application.commands.ApplyOrganizationBankingRestriction.ApplyOrganizationBankingRestrictionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class OrganizationComplianceStatusListenerTest {
    @Test
    void delegates_suspension_to_the_restriction_command() {
        ApplyOrganizationBankingRestrictionHandler handler = mock(ApplyOrganizationBankingRestrictionHandler.class);
        String message = "{\"eventType\":\"OrganizationComplianceSuspendedEvent\",\"event\":{"
                + "\"eventId\":\"event-1\",\"aggregateId\":1,\"occurredAt\":null,\"correlationId\":null,"
                + "\"payload\":{\"organizationId\":10,\"anchorBusinessCustomerId\":\"live-customer\","
                + "\"reason\":\"Review\",\"suspendedAt\":null}}}";
        // Avoid tracker access while preserving the event's operation id in the typed payload.
        message = message.replace("\"eventId\":\"event-1\"", "\"eventId\":null");

        new OrganizationComplianceStatusListener(new ObjectMapper(), handler).listen(message);

        ArgumentCaptor<ApplyOrganizationBankingRestrictionCommand> command =
                ArgumentCaptor.forClass(ApplyOrganizationBankingRestrictionCommand.class);
        verify(handler).execute(command.capture());
        assertTrue(command.getValue().restricted());
    }
}
