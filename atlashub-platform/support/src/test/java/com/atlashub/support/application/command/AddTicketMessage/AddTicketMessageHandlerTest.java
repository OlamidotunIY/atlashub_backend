package com.atlashub.support.application.command.AddTicketMessage;

import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.entities.TicketMessage;
import com.atlashub.support.domain.exceptions.UnauthorizedTicketActionException;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import com.atlashub.support.domain.repositories.TicketMessageRepository;
import com.atlashub.support.domain.valueobject.AuthorType;
import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.domain.valueobject.TicketStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddTicketMessageHandlerTest {

    @Mock
    private SupportTicketRepository ticketRepository;

    @Mock
    private TicketMessageRepository messageRepository;

    private AddTicketMessageHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AddTicketMessageHandler(ticketRepository, messageRepository);
    }

    @Test
    @DisplayName("Should throw UnauthorizedTicketActionException if customer attempts to add internal note")
    void shouldRejectCustomerInternalNote() {
        SupportTicket ticket = SupportTicket.create(
                1L, "TKT-2026-00001", 100L, 50L,
                TicketCategory.GENERAL, TicketPriority.LOW,
                "Inquiry", null, null, 1440, 4320, "corr-1"
        );
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        AddTicketMessageCommand command = new AddTicketMessageCommand(
                1L, 50L, AuthorType.CUSTOMER, "Secret note", true, null, "corr-1"
        );

        assertThrows(UnauthorizedTicketActionException.class, () -> handler.execute(command));
    }

    @Test
    @DisplayName("Should add message and resume ticket when customer responds to pending ticket")
    void shouldAddMessageAndResumeTicket() {
        SupportTicket ticket = SupportTicket.create(
                1L, "TKT-2026-00001", 100L, 50L,
                TicketCategory.GENERAL, TicketPriority.LOW,
                "Inquiry", null, null, 1440, 4320, "corr-1"
        );
        ticket.assign(900L, "corr-1");
        ticket.startProgress();
        ticket.awaitCustomerResponse();
        assertEquals(TicketStatus.PENDING_CUSTOMER_RESPONSE, ticket.getStatus());

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(messageRepository.nextIdentity()).thenReturn(202L);
        when(messageRepository.save(any(TicketMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddTicketMessageCommand command = new AddTicketMessageCommand(
                1L, 50L, AuthorType.CUSTOMER, "Here is the extra information", false, List.of("https://cdn.example.com/receipt.pdf"), "corr-msg"
        );

        AddTicketMessageResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(202L, result.messageId());
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());

        verify(ticketRepository).save(ticket);
        verify(messageRepository).save(any(TicketMessage.class));
    }
}
