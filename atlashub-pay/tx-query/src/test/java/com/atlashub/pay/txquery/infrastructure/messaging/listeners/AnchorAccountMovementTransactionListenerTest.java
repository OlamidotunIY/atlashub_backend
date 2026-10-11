package com.atlashub.pay.txquery.infrastructure.messaging.listeners;

import com.atlashub.pay.txquery.application.commands.ProjectAccountFunding.ProjectAccountFundingHandler;
import com.atlashub.pay.txquery.application.commands.ProjectPayoutTransaction.ProjectPayoutTransactionCommand;
import com.atlashub.pay.txquery.application.commands.ProjectPayoutTransaction.ProjectPayoutTransactionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AnchorAccountMovementTransactionListenerTest {
    @Test
    void projects_completed_anchor_debit_as_outbound_payout() {
        ProjectPayoutTransactionHandler payoutHandler = mock(ProjectPayoutTransactionHandler.class);
        ProjectAccountFundingHandler fundingHandler = mock(ProjectAccountFundingHandler.class);
        String message = "{\"eventType\":\"OrganizationAccountDebitedEvent\",\"event\":{"
                + "\"eventId\":null,\"aggregateId\":1,\"occurredAt\":null,\"correlationId\":null,"
                + "\"payload\":{\"organizationId\":2,\"environment\":\"LIVE\",\"businessAccountId\":3,"
                + "\"anchorTransferId\":\"anchor-transfer\",\"transferReference\":\"business-ref\","
                + "\"amount\":3000,\"currency\":\"NGN\",\"recipientName\":\"Ada Vendor\","
                + "\"maskedRecipientAccountNumber\":\"******7890\",\"recipientBankCode\":\"090405\","
                + "\"narration\":\"Supplier payment\",\"completedAt\":null}}}";

        new AnchorAccountMovementTransactionListener(new ObjectMapper(), payoutHandler, fundingHandler)
                .receive(message);

        ArgumentCaptor<ProjectPayoutTransactionCommand> command =
                ArgumentCaptor.forClass(ProjectPayoutTransactionCommand.class);
        verify(payoutHandler).execute(command.capture());
        assertEquals("business-ref", command.getValue().reference());
        assertEquals("******7890", command.getValue().recipientAccountNumber());
        assertEquals("090405", command.getValue().metadata().get("recipientBankCode"));
        verifyNoInteractions(fundingHandler);
    }
}
