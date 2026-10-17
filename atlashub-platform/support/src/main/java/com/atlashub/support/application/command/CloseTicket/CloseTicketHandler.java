package com.atlashub.support.application.command.CloseTicket;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.exceptions.TicketNotFoundException;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import org.springframework.stereotype.Component;

@Component
public class CloseTicketHandler extends Command<CloseTicketCommand, CloseTicketResult> {

    private final SupportTicketRepository ticketRepository;

    public CloseTicketHandler(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public CloseTicketResult execute(CloseTicketCommand command) {
        SupportTicket ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(command.ticketId()));

        ticket.close(command.closedBy(), command.correlationId());
        SupportTicket savedTicket = ticketRepository.save(ticket);

        return new CloseTicketResult(
                savedTicket.getId(),
                savedTicket.getStatus().name(),
                savedTicket.getClosedAt(),
                savedTicket.getClosedBy()
        );
    }
}
