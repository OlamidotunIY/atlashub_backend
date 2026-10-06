package com.atlashub.pay.accounts.infrastructure.messaging.listeners;

import com.atlashub.pay.accounts.application.commands.ProvisionOrganizationBanking.ProvisionOrganizationBankingCommand;
import com.atlashub.pay.accounts.application.commands.ProvisionOrganizationBanking.ProvisionOrganizationBankingHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrganizationTestBankingReadyListenerTest {
    @Test
    void delegates_test_readiness_to_the_provisioning_command() {
        ProvisionOrganizationBankingHandler handler = mock(ProvisionOrganizationBankingHandler.class);
        String message = "{\"eventType\":\"OrganizationTestBankingReadyEvent\",\"event\":{"
                + "\"eventId\":null,\"aggregateId\":1,\"occurredAt\":null,\"correlationId\":null,"
                + "\"payload\":{\"organizationId\":10,\"anchorBusinessCustomerId\":\"sandbox-customer\","
                + "\"environment\":\"TEST\",\"readyAt\":null}}}";

        new OrganizationTestBankingReadyListener(new ObjectMapper(), handler).listen(message);

        ArgumentCaptor<ProvisionOrganizationBankingCommand> command =
                ArgumentCaptor.forClass(ProvisionOrganizationBankingCommand.class);
        verify(handler).execute(command.capture());
        assertEquals("TEST", command.getValue().environment());
        assertEquals("sandbox-customer", command.getValue().anchorBusinessCustomerId());
    }
}
