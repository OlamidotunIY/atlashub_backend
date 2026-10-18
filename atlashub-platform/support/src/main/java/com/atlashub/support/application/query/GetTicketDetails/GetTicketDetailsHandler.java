package com.atlashub.support.application.query.GetTicketDetails;

import com.atlashub.shared.application.usecase.Query;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.entities.TicketMessage;
import com.atlashub.support.domain.exceptions.TicketNotFoundException;
import com.atlashub.support.domain.exceptions.UnauthorizedTicketActionException;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import com.atlashub.support.domain.repositories.TicketMessageRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GetTicketDetailsHandler extends Query<GetTicketDetailsQuery, TicketDetailsResult> {

    private final SupportTicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;

    public GetTicketDetailsHandler(
            SupportTicketRepository ticketRepository,
            TicketMessageRepository messageRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public TicketDetailsResult execute(GetTicketDetailsQuery query) {
        SupportTicket ticket = ticketRepository.findById(query.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(query.ticketId()));

        if (!query.isAgent() && query.requesterOrgId() != null && !ticket.getOrganizationId().equals(query.requesterOrgId())) {
            throw new UnauthorizedTicketActionException("Cannot access tickets belonging to another organization");
        }

        List<TicketMessage> messages = messageRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId());

        List<TicketMessageResult> messageResults = messages.stream()
                .filter(m -> query.isAgent() || !m.isInternalNote())
                .map(m -> new TicketMessageResult(
                        m.getId(),
                        m.getTicketId(),
                        m.getAuthorId(),
                        m.getAuthorType(),
                        m.getMessage(),
                        m.isInternalNote(),
                        m.getAttachmentUrls(),
                        m.getCreatedAt()
                ))
                .toList();

        return new TicketDetailsResult(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getOrganizationId(),
                ticket.getRaisedBy(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getSubject(),
                ticket.getStatus(),
                ticket.getAssignedTo(),
                ticket.isSlaBreached(),
                ticket.getRelatedEntityType(),
                ticket.getRelatedEntityId(),
                ticket.getFirstResponseDeadline(),
                ticket.getResolutionDeadline(),
                ticket.getFirstRespondedAt(),
                ticket.getResolvedAt(),
                ticket.getResolutionNote(),
                ticket.getClosedAt(),
                ticket.getClosedBy(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                messageResults
        );
    }
}
