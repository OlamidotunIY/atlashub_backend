package com.atlashub.support.domain.repositories;

import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.domain.valueobject.TicketStatus;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository extends Repository<SupportTicket> {

    Optional<SupportTicket> findByTicketNumber(String ticketNumber);

    List<SupportTicket> findByOrganizationId(Long organizationId);

    List<SupportTicket> findByOrganizationIdAndStatus(Long organizationId, TicketStatus status);

    List<SupportTicket> findByAssignedToAndStatus(Long agentId, TicketStatus status);

    List<SupportTicket> findSlaOverdueTickets(ZonedDateTime now);

    List<SupportTicket> findIdleResolvedTickets(ZonedDateTime cutoff);

    List<SupportTicket> searchTickets(
            TicketStatus status,
            TicketCategory category,
            TicketPriority priority,
            Boolean slaBreached
    );

    List<SupportTicket> findCreatedBetween(ZonedDateTime from, ZonedDateTime to);
}
