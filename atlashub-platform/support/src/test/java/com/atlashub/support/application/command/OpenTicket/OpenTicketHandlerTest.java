package com.atlashub.support.application.command.OpenTicket;

import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.entities.TicketMessage;
import com.atlashub.support.domain.entities.TicketSlaConfig;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import com.atlashub.support.domain.repositories.TicketMessageRepository;
import com.atlashub.support.domain.repositories.TicketSlaConfigRepository;
import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenTicketHandlerTest {

    @Mock
    private SupportTicketRepository ticketRepository;

    @Mock
    private TicketMessageRepository messageRepository;

    @Mock
    private TicketSlaConfigRepository slaConfigRepository;

    private OpenTicketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OpenTicketHandler(ticketRepository, messageRepository, slaConfigRepository);
    }

    @Test
    @DisplayName("Should open ticket, compute SLA deadlines, save ticket and initial message")
    void shouldOpenTicketSuccessfully() {
        OpenTicketCommand command = new OpenTicketCommand(
                100L,
                50L,
                TicketCategory.PAYMENT_DISPUTE,
                TicketPriority.CRITICAL,
                "Double charge on order",
                "I was charged twice for order 123",
                "Charge",
                "chg_123",
                "corr-1"
        );

        TicketSlaConfig slaConfig = new TicketSlaConfig(1L, TicketCategory.PAYMENT_DISPUTE, TicketPriority.CRITICAL, 30, 240);
        when(slaConfigRepository.findByCategoryAndPriority(command.category(), command.priority()))
                .thenReturn(Optional.of(slaConfig));
        when(ticketRepository.nextIdentity()).thenReturn(101L);
        when(ticketRepository.save(any(SupportTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.nextIdentity()).thenReturn(201L);

        OpenTicketResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(101L, result.ticketId());
        assertNotNull(result.ticketNumber());

        verify(ticketRepository).save(any(SupportTicket.class));
        verify(messageRepository).save(any(TicketMessage.class));
    }
}
