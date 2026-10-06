package com.atlashub.compliance.infrastructure.messaging.listeners;

import com.atlashub.compliance.application.commands.ProvisionTestAnchorCustomer.ProvisionTestAnchorCustomerCommand;
import com.atlashub.compliance.application.commands.ProvisionTestAnchorCustomer.ProvisionTestAnchorCustomerHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ComplianceSubmittedTestBankingListenerTest {
    @Test
    void delegates_submission_to_the_test_customer_command() {
        ProvisionTestAnchorCustomerHandler handler = mock(ProvisionTestAnchorCustomerHandler.class);
        String message = "{\"eventType\":\"ComplianceSubmittedEvent\",\"event\":{"
                + "\"eventId\":null,\"aggregateId\":1,\"occurredAt\":null,\"correlationId\":null,"
                + "\"payload\":{\"organizationId\":10,\"submittedAt\":null}}}";

        new ComplianceSubmittedTestBankingListener(new ObjectMapper(), handler).listen(message);

        ArgumentCaptor<ProvisionTestAnchorCustomerCommand> command =
                ArgumentCaptor.forClass(ProvisionTestAnchorCustomerCommand.class);
        verify(handler).execute(command.capture());
        assertEquals(10L, command.getValue().organizationId());
    }
}
