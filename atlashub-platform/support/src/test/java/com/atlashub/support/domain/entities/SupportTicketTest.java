package com.atlashub.support.domain.entities;

import com.atlashub.support.domain.events.TicketAssignedEvent;
import com.atlashub.support.domain.events.TicketClosedEvent;
import com.atlashub.support.domain.events.TicketCreatedEvent;
import com.atlashub.support.domain.events.TicketEscalatedEvent;
import com.atlashub.support.domain.events.TicketReopenedEvent;
import com.atlashub.support.domain.events.TicketResolvedEvent;
import com.atlashub.support.domain.events.TicketSlaBreachedEvent;
import com.atlashub.support.domain.exceptions.InvalidTicketStateException;
import com.atlashub.support.domain.exceptions.TicketAlreadyClosedException;
import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.domain.valueobject.TicketStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupportTicketTest {

    @Test
    @DisplayName("Should create support ticket in OPEN status with calculated SLA deadlines and register event")
    void shouldCreateTicketSuccessfully() {
        SupportTicket ticket = SupportTicket.create(
                1L, "TKT-2026-00001", 100L, 50L,
                TicketCategory.PAYMENT_DISPUTE, TicketPriority.HIGH,
                "Double charge on order #123", "Charge", "chg_999",
                120, 1440, "corr-1"
        );

        assertNotNull(ticket);
        assertEquals(TicketStatus.OPEN, ticket.getStatus());
        assertEquals("TKT-2026-00001", ticket.getTicketNumber());
        assertEquals(100L, ticket.getOrganizationId());
        assertEquals(50L, ticket.getRaisedBy());
        assertFalse(ticket.isSlaBreached());
        assertNotNull(ticket.getFirstResponseDeadline());
        assertNotNull(ticket.getResolutionDeadline());

        assertFalse(ticket.peekDomainEvents().isEmpty());
        assertTrue(ticket.peekDomainEvents().get(0) instanceof TicketCreatedEvent);
    }

    @Test
    @DisplayName("Should transition OPEN to ASSIGNED and record first response time upon assignment")
    void shouldAssignTicket() {
        SupportTicket ticket = SupportTicket.create(
                1L, "TKT-2026-00001", 100L, 50L,
                TicketCategory.TECHNICAL, TicketPriority.MEDIUM,
                "API webhook failure", null, null,
                240, 2880, "corr-1"
        );

        ticket.assign(900L, "corr-assign");

        assertEquals(TicketStatus.ASSIGNED, ticket.getStatus());
        assertEquals(900L, ticket.getAssignedTo());
        assertNotNull(ticket.getFirstRespondedAt());

        boolean hasAssignedEvent = ticket.peekDomainEvents().stream()
                .anyMatch(e -> e instanceof TicketAssignedEvent);
        assertTrue(hasAssignedEvent);
    }

    @Test
    @DisplayName("Should progress through active thread states: ASSIGNED -> IN_PROGRESS -> PENDING_CUSTOMER_RESPONSE -> resume")
    void shouldProgressThroughThreadStates() {
        SupportTicket ticket = SupportTicket.create(
                1L, "TKT-2026-00001", 100L, 50L,
                TicketCategory.GENERAL, TicketPriority.LOW,
                "General question", null, null,
                1440, 4320, "corr-1"
        );

        ticket.assign(900L, "corr-1");
        ticket.startProgress();
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());

        ticket.awaitCustomerResponse();
        assertEquals(TicketStatus.PENDING_CUSTOMER_RESPONSE, ticket.getStatus());

        ticket.resume();
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
    }

    @Test
    @DisplayName("Should resolve ticket from IN_PROGRESS and register TicketResolvedEvent")
    void shouldResolveTicket() {
        SupportTicket ticket = SupportTicket.create(
                1L, "TKT-2026-00001", 100L, 50L,
                TicketCategory.BILLING, TicketPriority.HIGH,
                "Billing discrepancy", null, null,
                120, 1440, "corr-1"
        );
        ticket.assign(900L, "corr-1");
        ticket.startProgress();

        ticket.resolve(900L, "Issue verified and refund initiated", "corr-res");

        assertEquals(TicketStatus.RESOLVED, ticket.getStatus());
        assertNotNull(ticket.getResolvedAt());
        assertEquals("Issue verified and refund initiated", ticket.getResolutionNote());

        boolean hasResolvedEvent = ticket.peekDomainEvents().stream()
                .anyMatch(e -> e instanceof TicketResolvedEvent);
        assertTrue(hasResolvedEvent);
    }

    @Test
    @DisplayName("Should throw InvalidTicketStateException when attempting to resolve ticket not IN_PROGRESS")
    void shouldRejectResolveWhenNotInProgress() {
        SupportTicket ticket = SupportTicket.create(
                1L, "TKT-2026-00001", 100L, 50L,
                TicketCategory.BILLING, TicketPriority.HIGH,
                "Billing discrepancy", null, null,
                120, 1440, "corr-1"
        );

        assertThrows(InvalidTicketStateException.class, () ->
                ticket.resolve(900L, "Note", "corr-1"));
    }

    @Test
    @DisplayName("Should close and reopen ticket lifecycle")
    void shouldCloseAndReopenTicket() {
        SupportTicket ticket = SupportTicket.create(
                1L, "TKT-2026-00001", 100L, 50L,
                TicketCategory.GENERAL, TicketPriority.LOW,
                "Inquiry", null, null,
                1440, 4320, "corr-1"
        );
        ticket.assign(900L, "corr-1");
        ticket.startProgress();
        ticket.resolve(900L, "Answer provided", "corr-1");

        ticket.close(50L, "corr-close");
        assertEquals(TicketStatus.CLOSED, ticket.getStatus());
        assertNotNull(ticket.getClosedAt());

        // Attempting to close again throws TicketAlreadyClosedException
        assertThrows(TicketAlreadyClosedException.class, () -> ticket.close(50L, "corr-2"));

        ticket.reopen(50L, "Customer has further questions", "corr-reopen");
        assertEquals(TicketStatus.OPEN, ticket.getStatus());
        assertNull(ticket.getResolvedAt());
        assertNull(ticket.getClosedAt());
    }

    @Test
    @DisplayName("Should escalate ticket and mark SLA breached")
    void shouldEscalateAndMarkSlaBreached() {
        SupportTicket ticket = SupportTicket.create(
                1L, "TKT-2026-00001", 100L, 50L,
                TicketCategory.PAYMENT_DISPUTE, TicketPriority.CRITICAL,
                "Urgent charge dispute", null, null,
                30, 240, "corr-1"
        );

        ticket.escalate(999L, "Escalating to lead finance agent", "corr-esc");
        assertEquals(999L, ticket.getAssignedTo());

        ticket.markSlaBreached("DEADLINE_PASSED", "corr-sla");
        assertTrue(ticket.isSlaBreached());

        boolean hasBreachedEvent = ticket.peekDomainEvents().stream()
                .anyMatch(e -> e instanceof TicketSlaBreachedEvent);
        assertTrue(hasBreachedEvent);
    }
}
