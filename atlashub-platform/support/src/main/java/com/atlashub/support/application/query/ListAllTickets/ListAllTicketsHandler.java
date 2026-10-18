package com.atlashub.support.application.query.ListAllTickets;

import com.atlashub.shared.application.usecase.Query;
import com.atlashub.support.application.query.ListMyTickets.TicketSummaryResult;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ListAllTicketsHandler extends Query<ListAllTicketsQuery, List<TicketSummaryResult>> {

    private final SupportTicketRepository ticketRepository;

    public ListAllTicketsHandler(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public List<TicketSummaryResult> execute(ListAllTicketsQuery query) {
        List<SupportTicket> tickets = ticketRepository.searchTickets(
                query.status(),
                query.category(),
                query.priority(),
                query.slaBreached()
        );

        return tickets.stream()
                .map(this::toSummary)
                .toList();
    }

    private TicketSummaryResult toSummary(SupportTicket t) {
        return new TicketSummaryResult(
                t.getId(),
                t.getTicketNumber(),
                t.getOrganizationId(),
                t.getRaisedBy(),
                t.getCategory(),
                t.getPriority(),
                t.getSubject(),
                t.getStatus(),
                t.getAssignedTo(),
                t.isSlaBreached(),
                t.getFirstResponseDeadline(),
                t.getResolutionDeadline(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
