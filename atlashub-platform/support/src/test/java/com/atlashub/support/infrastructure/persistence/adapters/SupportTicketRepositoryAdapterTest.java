package com.atlashub.support.infrastructure.persistence.adapters;

import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.domain.valueobject.TicketStatus;
import com.atlashub.support.infrastructure.persistence.entities.SupportTicketJpa;
import com.atlashub.support.infrastructure.persistence.mappers.SupportTicketMapper;
import com.atlashub.support.infrastructure.persistence.repositories.SpringDataSupportTicketRepository;
import java.time.ZonedDateTime;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportTicketRepositoryAdapterTest {

    @Mock
    private SpringDataSupportTicketRepository springDataRepo;

    @Mock
    private SupportTicketMapper mapper;

    @Mock
    private DomainSequenceGenerator sequenceGenerator;

    @Mock
    private DomainEventPublisher eventPublisher;

    private SupportTicketRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SupportTicketRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should find ticket by ticket number")
    void shouldFindByTicketNumber() {
        SupportTicketJpa jpa = mock(SupportTicketJpa.class);
        SupportTicket domain = mock(SupportTicket.class);

        when(springDataRepo.findByTicketNumber("TKT-2026-00001")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<SupportTicket> result = adapter.findByTicketNumber("TKT-2026-00001");

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should find tickets by organization ID")
    void shouldFindByOrganizationId() {
        SupportTicketJpa jpa = mock(SupportTicketJpa.class);
        SupportTicket domain = mock(SupportTicket.class);

        when(springDataRepo.findByOrganizationId(100L)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<SupportTicket> result = adapter.findByOrganizationId(100L);

        assertEquals(1, result.size());
        assertEquals(domain, result.get(0));
    }

    @Test
    @DisplayName("Should find SLA overdue tickets")
    void shouldFindSlaOverdueTickets() {
        SupportTicketJpa jpa = mock(SupportTicketJpa.class);
        SupportTicket domain = mock(SupportTicket.class);
        ZonedDateTime now = ZonedDateTime.now();

        when(springDataRepo.findSlaOverdueTickets(now)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<SupportTicket> result = adapter.findSlaOverdueTickets(now);

        assertEquals(1, result.size());
        assertEquals(domain, result.get(0));
    }

    @Test
    @DisplayName("Should return configured sequence name")
    void shouldReturnSequenceName() {
        assertEquals("support_ticket_seq", adapter.getSequenceName());
    }
}
