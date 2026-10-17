package com.atlashub.support.infrastructure.persistence.adapters;

import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.domain.valueobject.TicketStatus;
import com.atlashub.support.infrastructure.persistence.entities.SupportTicketJpa;
import com.atlashub.support.infrastructure.persistence.mappers.SupportTicketMapper;
import com.atlashub.support.infrastructure.persistence.repositories.SpringDataSupportTicketRepository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SupportTicketRepositoryAdapter
        extends JpaBaseRepository<SupportTicket, SupportTicketJpa>
        implements SupportTicketRepository {

    private final SpringDataSupportTicketRepository springDataRepo;

    public SupportTicketRepositoryAdapter(
            SpringDataSupportTicketRepository springDataRepo,
            SupportTicketMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "support_ticket_seq";
    }

    @Override
    public Optional<SupportTicket> findByTicketNumber(String ticketNumber) {
        return springDataRepo.findByTicketNumber(ticketNumber).map(mapper::toDomain);
    }

    @Override
    public List<SupportTicket> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<SupportTicket> findByOrganizationIdAndStatus(Long organizationId, TicketStatus status) {
        return springDataRepo.findByOrganizationIdAndStatus(organizationId, status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<SupportTicket> findByAssignedToAndStatus(Long agentId, TicketStatus status) {
        return springDataRepo.findByAssignedToAndStatus(agentId, status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<SupportTicket> findSlaOverdueTickets(ZonedDateTime now) {
        return springDataRepo.findSlaOverdueTickets(now).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<SupportTicket> findIdleResolvedTickets(ZonedDateTime cutoff) {
        return springDataRepo.findIdleResolvedTickets(cutoff).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<SupportTicket> searchTickets(
            TicketStatus status,
            TicketCategory category,
            TicketPriority priority,
            Boolean slaBreached
    ) {
        return springDataRepo.searchTickets(status, category, priority, slaBreached).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<SupportTicket> findCreatedBetween(ZonedDateTime from, ZonedDateTime to) {
        return springDataRepo.findCreatedBetween(from, to).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
