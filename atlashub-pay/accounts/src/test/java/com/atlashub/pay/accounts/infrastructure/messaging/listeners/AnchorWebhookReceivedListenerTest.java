package com.atlashub.pay.accounts.infrastructure.messaging.listeners;

import com.atlashub.pay.accounts.application.commands.ApplyAnchorAccountStatus.ApplyAnchorAccountStatusHandler;
import com.atlashub.pay.accounts.application.commands.RecordAnchorDebit.RecordAnchorDebitCommand;
import com.atlashub.pay.accounts.application.commands.RecordAnchorDebit.RecordAnchorDebitHandler;
import com.atlashub.pay.accounts.application.commands.RecordAnchorFunding.RecordAnchorFundingHandler;
import com.atlashub.pay.accounts.application.commands.RecordAnchorFunding.RecordAnchorFundingCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AnchorWebhookReceivedListenerTest {
    @Test
    void maps_successful_anchor_transfer_to_debit_command() {
        ApplyAnchorAccountStatusHandler statusHandler = mock(ApplyAnchorAccountStatusHandler.class);
        RecordAnchorFundingHandler fundingHandler = mock(RecordAnchorFundingHandler.class);
        RecordAnchorDebitHandler debitHandler = mock(RecordAnchorDebitHandler.class);
        String message = """
                {"eventType":"AnchorWebhookReceivedEvent","event":{
                  "eventId":null,"environment":"LIVE","consumer":"PAY_ACCOUNTS",
                  "eventType":"nip.transfer.successful","occurredAt":null,"attributes":{},
                  "relationships":{"account":{"id":"anchor-account","type":"DepositAccount"}},
                  "includedResources":[
                    {"id":"anchor-transfer","type":"NIP_TRANSFER",
                     "attributes":{"reference":"business-ref","amount":3000,"currency":"NGN","reason":"Supplier payment"},
                     "relationships":{"account":{"id":"anchor-account","type":"DepositAccount"},
                                      "counterParty":{"id":"counterparty-1","type":"CounterParty"}}},
                    {"id":"counterparty-1","type":"CounterParty",
                     "attributes":{"accountName":"Ada Vendor","accountNumber":"1234567890",
                                   "bank":{"bankCode":"090405"}},"relationships":{}}
                  ]}}
                """;

        new AnchorWebhookReceivedListener(new ObjectMapper(), statusHandler, fundingHandler, debitHandler)
                .listen(message);

        ArgumentCaptor<RecordAnchorDebitCommand> command = ArgumentCaptor.forClass(RecordAnchorDebitCommand.class);
        verify(debitHandler).execute(command.capture());
        assertEquals("anchor-account", command.getValue().depositAccountId());
        assertEquals("anchor-transfer", command.getValue().anchorTransferId());
        assertEquals("business-ref", command.getValue().transferReference());
        assertEquals("Ada Vendor", command.getValue().recipientName());
        assertFalse(command.getValue().reversed());
        verifyNoInteractions(fundingHandler, statusHandler);
    }

    @Test
    void maps_completed_inbound_nip_transfer_to_funding_command() {
        ApplyAnchorAccountStatusHandler statusHandler = mock(ApplyAnchorAccountStatusHandler.class);
        RecordAnchorFundingHandler fundingHandler = mock(RecordAnchorFundingHandler.class);
        RecordAnchorDebitHandler debitHandler = mock(RecordAnchorDebitHandler.class);
        String message = """
                {"eventType":"AnchorWebhookReceivedEvent","event":{
                  "eventId":null,"environment":"LIVE","consumer":"PAY_ACCOUNTS",
                  "eventType":"nip.inbound.completed","occurredAt":null,"attributes":{},
                  "relationships":{"account":{"id":"anchor-account","type":"DepositAccount"}},
                  "includedResources":[
                    {"id":"anchor-inbound","type":"InboundNIPTransfer",
                     "attributes":{"reference":"inbound-ref","amount":5000,"currency":"NGN",
                                   "sourceAccountName":"Ada Customer",
                                   "sourceBank":{"name":"Example Bank","nipCode":"090405"},
                                   "description":"Invoice payment"},
                     "relationships":{"account":{"id":"anchor-account","type":"DepositAccount"}}}
                  ]}}
                """;

        new AnchorWebhookReceivedListener(new ObjectMapper(), statusHandler, fundingHandler, debitHandler)
                .listen(message);

        ArgumentCaptor<RecordAnchorFundingCommand> command =
                ArgumentCaptor.forClass(RecordAnchorFundingCommand.class);
        verify(fundingHandler).execute(command.capture());
        assertEquals("anchor-account", command.getValue().depositAccountId());
        assertEquals("inbound-ref", command.getValue().transferReference());
        assertEquals("Ada Customer", command.getValue().senderAccountName());
        assertEquals("090405", command.getValue().senderBankCode());
        verifyNoInteractions(debitHandler, statusHandler);
    }
}
