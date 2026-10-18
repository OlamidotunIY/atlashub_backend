package com.atlashub.support.application.command.ResolveTicket;

import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.domain.valueobject.TicketStatus;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolveTicketHandlerTest {

    @Mock
    private SupportTicketRepository ticketRepository;

    private ResolveTicketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ResolveTicketHandler(ticketRepository);
    }

    @Test
    @DisplayName("Should resolve ticket and transition to RESOLVED")
    void shouldResolveTicket() {
        SupportTicket ticket = SupportTicket.create(
                1L, "TKT-2026-00001", 100L, 50L,
                TicketCategory.TECHNICAL, TicketPriority.HIGH,
                "API Error", null, null, 120, 1440, "corr-1"
        );
        ticket.assign(900L, "corr-1");
        ticket.startProgress();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        ResolveTicketCommand command = new ResolveTicketCommand(1L, 900L, "Fixed configuration", "corr-res");
        ResolveTicketResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(TicketStatus.RESOLVED.name(), result.status());
        assertEquals("Fixed configuration", result.resolutionNote());
        assertNotNull(result.resolvedAt());

        verify(ticketRepository).save(ticket);
    }
}
