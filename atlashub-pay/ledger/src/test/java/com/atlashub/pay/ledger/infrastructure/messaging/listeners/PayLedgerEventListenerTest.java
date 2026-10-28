package com.atlashub.pay.ledger.infrastructure.messaging.listeners;

import com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent.ProcessLedgerEventCommand;
import com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent.ProcessLedgerEventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PayLedgerEventListenerTest {
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

    @Test
    void posts_successful_charge_net_of_provider_fee() {
        ProcessLedgerEventHandler handler = mock(ProcessLedgerEventHandler.class);
        String message = "{\"eventType\":\"ChargeSuccessfulEvent\",\"event\":{"
                + "\"eventId\":null,\"aggregateId\":1,\"occurredAt\":null,\"correlationId\":null,"
                + "\"payload\":{\"organizationId\":2,\"environment\":\"LIVE\","
                + "\"chargeReference\":\"charge-1\",\"gatewayReference\":\"gateway-1\","
                + "\"amount\":{\"amount\":10000,\"currency\":\"NGN\"},"
                + "\"providerFee\":{\"amount\":500,\"currency\":\"NGN\"},"
                + "\"channel\":\"CARD\",\"provider\":\"PAYSTACK\","
                + "\"sourceSystem\":\"COMMERCE\",\"sourceReferenceId\":\"order-1\","
                + "\"customerReferenceId\":\"customer-1\",\"succeededAt\":null}}}";

        new PayLedgerEventListener(new ObjectMapper(), handler).chargeSuccessful(message);

        ArgumentCaptor<ProcessLedgerEventCommand> command = ArgumentCaptor.forClass(ProcessLedgerEventCommand.class);
        verify(handler).execute(command.capture());
        assertEquals(0, new java.math.BigDecimal("9500").compareTo(command.getValue().amount()));
    }

    @Test
    void maps_completed_anchor_debit_and_reversal() {
        ProcessLedgerEventHandler handler = mock(ProcessLedgerEventHandler.class);
        String debit = "{\"eventType\":\"OrganizationAccountDebitedEvent\",\"event\":{"
                + "\"eventId\":null,\"aggregateId\":1,\"occurredAt\":null,\"correlationId\":null,"
                + "\"payload\":{\"organizationId\":2,\"environment\":\"LIVE\",\"businessAccountId\":3,"
                + "\"anchorTransferId\":\"anchor-transfer\",\"transferReference\":\"business-ref\","
                + "\"amount\":3000,\"currency\":\"NGN\",\"completedAt\":null}}}";
        String reversal = debit.replace("OrganizationAccountDebitedEvent", "OrganizationAccountDebitReversedEvent")
                .replace("\"completedAt\"", "\"reversedAt\"");
        PayLedgerEventListener listener = new PayLedgerEventListener(new ObjectMapper(), handler);

        listener.organizationDebited(debit);
        listener.organizationDebitReversed(reversal);

        ArgumentCaptor<ProcessLedgerEventCommand> command = ArgumentCaptor.forClass(ProcessLedgerEventCommand.class);
        verify(handler, times(2)).execute(command.capture());
        assertEquals(ProcessLedgerEventCommand.Action.ORGANIZATION_ACCOUNT_DEBITED,
                command.getAllValues().getFirst().action());
        assertEquals(ProcessLedgerEventCommand.Action.ORGANIZATION_ACCOUNT_DEBIT_REVERSED,
                command.getAllValues().getLast().action());
        assertTrue(command.getAllValues().getLast().reference().startsWith("reversal:"));
    }
}
