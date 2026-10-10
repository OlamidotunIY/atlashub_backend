package com.atlashub.pay.accounts.infrastructure.messaging.listeners;

import com.atlashub.pay.accounts.application.commands.InitializeTestPaymentProfile.InitializeTestPaymentProfileCommand;
import com.atlashub.pay.accounts.application.commands.InitializeTestPaymentProfile.InitializeTestPaymentProfileHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OrganizationRegisteredPaymentProfileListenerTest {
    @Test
    void initializes_the_test_payment_profile_from_registration() {
        InitializeTestPaymentProfileHandler handler = mock(InitializeTestPaymentProfileHandler.class);
        String message = "{\"eventType\":\"OrganizationRegistered\",\"event\":{"
                + "\"eventId\":null,\"aggregateId\":10,\"occurredAt\":null,\"correlationId\":null,\"payload\":{}}}";

        new OrganizationRegisteredPaymentProfileListener(new ObjectMapper(), handler).listen(message);

        ArgumentCaptor<InitializeTestPaymentProfileCommand> command =
                ArgumentCaptor.forClass(InitializeTestPaymentProfileCommand.class);
        verify(handler).execute(command.capture());
        assertEquals(10L, command.getValue().organizationId());
    }
}
