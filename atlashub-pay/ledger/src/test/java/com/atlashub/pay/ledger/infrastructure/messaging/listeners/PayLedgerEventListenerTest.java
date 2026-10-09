package com.atlashub.pay.ledger.infrastructure.messaging.listeners;

import com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent.ProcessLedgerEventCommand;
import com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent.ProcessLedgerEventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PayLedgerEventListenerTest {
    @Test
    void bootstraps_test_and_live_ledgers_when_an_organization_registers() {
        ProcessLedgerEventHandler handler = mock(ProcessLedgerEventHandler.class);
        String message = "{\"eventType\":\"OrganizationRegistered\",\"event\":{"
                + "\"eventId\":null,\"aggregateId\":2,\"occurredAt\":null,\"correlationId\":null,"
                + "\"payload\":{\"currency\":\"NGN\"}}}";

        new PayLedgerEventListener(new ObjectMapper(), handler).organizationRegistered(message);

        ArgumentCaptor<ProcessLedgerEventCommand> command = ArgumentCaptor.forClass(ProcessLedgerEventCommand.class);
        verify(handler, times(2)).execute(command.capture());
        assertEquals("TEST", command.getAllValues().getFirst().environment());
        assertEquals("LIVE", command.getAllValues().getLast().environment());
        assertEquals(ProcessLedgerEventCommand.Action.BOOTSTRAP_ORGANIZATION,
                command.getAllValues().getFirst().action());
    }

    @Test
    void maps_reserved_account_funding_envelope_to_command() {
        ProcessLedgerEventHandler handler = mock(ProcessLedgerEventHandler.class);
        String message = "{\"eventType\":\"ReservedAccountFundedEvent\",\"event\":{"
                + "\"eventId\":null,\"aggregateId\":1,\"occurredAt\":null,\"correlationId\":null,"
                + "\"payload\":{\"reservedAccountId\":1,\"organizationId\":2,\"environment\":\"TEST\","
                + "\"ownerType\":\"CUSTOMER\",\"ownerReferenceId\":\"customer-1\","
                + "\"businessSubAccountId\":3,\"anchorTransferReference\":\"transfer-1\","
                + "\"amount\":1000,\"currency\":\"NGN\",\"senderAccountName\":\"Ada\","
                + "\"senderBankCode\":\"090405\",\"receivedAt\":null}}}";

        new PayLedgerEventListener(new ObjectMapper(), handler).reservedFunded(message);

        ArgumentCaptor<ProcessLedgerEventCommand> command = ArgumentCaptor.forClass(ProcessLedgerEventCommand.class);
        verify(handler).execute(command.capture());
        assertEquals(ProcessLedgerEventCommand.Action.RESERVED_ACCOUNT_FUNDED, command.getValue().action());
        assertEquals("TEST", command.getValue().environment());
        assertEquals("transfer-1", command.getValue().reference());
    }
}
