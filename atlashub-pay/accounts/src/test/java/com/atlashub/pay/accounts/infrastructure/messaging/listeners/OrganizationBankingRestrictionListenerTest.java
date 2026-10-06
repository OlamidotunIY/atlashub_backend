package com.atlashub.pay.accounts.infrastructure.messaging.listeners;

import com.atlashub.pay.accounts.application.commands.ApplyOrganizationBankingRestriction.ApplyOrganizationBankingRestrictionCommand;
import com.atlashub.pay.accounts.application.commands.ApplyOrganizationBankingRestriction.ApplyOrganizationBankingRestrictionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class OrganizationBankingRestrictionListenerTest {
    @Test
    void delegates_organization_ban_with_its_own_restriction_source() {
        ApplyOrganizationBankingRestrictionHandler handler = mock(ApplyOrganizationBankingRestrictionHandler.class);
        String message = "{\"eventType\":\"OrganizationBannedEvent\",\"event\":{"
                + "\"eventId\":null,\"aggregateId\":1,\"occurredAt\":null,\"correlationId\":null,"
                + "\"payload\":{\"organizationId\":10,\"reason\":\"Risk\",\"bannedAt\":null}}}";

        new OrganizationBankingRestrictionListener(new ObjectMapper(), handler).listen(message);

        ArgumentCaptor<ApplyOrganizationBankingRestrictionCommand> command =
                ArgumentCaptor.forClass(ApplyOrganizationBankingRestrictionCommand.class);
        verify(handler).execute(command.capture());
        assertTrue(command.getValue().restricted());
        assertEquals("ORGANIZATION_BAN", command.getValue().restrictionType());
    }
}
