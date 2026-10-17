package com.atlashub.support.application.query.ListAgentTickets;

import com.atlashub.shared.application.usecase.Query;
import com.atlashub.support.application.query.ListMyTickets.TicketSummaryResult;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ListAgentTicketsHandler extends Query<ListAgentTicketsQuery, List<TicketSummaryResult>> {

    private final SupportTicketRepository ticketRepository;

    public ListAgentTicketsHandler(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public List<TicketSummaryResult> execute(ListAgentTicketsQuery query) {
        List<SupportTicket> tickets = ticketRepository.findByAssignedToAndStatus(query.agentId(), query.status());

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
