package com.atlashub.support.application.query.ListMyTickets;

import com.atlashub.shared.application.usecase.Query;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ListMyTicketsHandler extends Query<ListMyTicketsQuery, List<TicketSummaryResult>> {

    private final SupportTicketRepository ticketRepository;

    public ListMyTicketsHandler(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public List<TicketSummaryResult> execute(ListMyTicketsQuery query) {
        List<SupportTicket> tickets;
        if (query.status() != null) {
            tickets = ticketRepository.findByOrganizationIdAndStatus(query.organizationId(), query.status());
        } else {
            tickets = ticketRepository.findByOrganizationId(query.organizationId());
        }

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
