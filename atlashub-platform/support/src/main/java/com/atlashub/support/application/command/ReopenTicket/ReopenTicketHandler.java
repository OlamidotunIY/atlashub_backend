package com.atlashub.support.application.command.ReopenTicket;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.exceptions.TicketNotFoundException;
import com.atlashub.support.domain.exceptions.UnauthorizedTicketActionException;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import org.springframework.stereotype.Component;

@Component
public class ReopenTicketHandler extends Command<ReopenTicketCommand, ReopenTicketResult> {

    private final SupportTicketRepository ticketRepository;

    public ReopenTicketHandler(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public ReopenTicketResult execute(ReopenTicketCommand command) {
        SupportTicket ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(command.ticketId()));

        if (!ticket.getRaisedBy().equals(command.userId())) {
            throw new UnauthorizedTicketActionException("Only the user who raised the ticket can reopen it");
        }

        ticket.reopen(command.userId(), command.reason(), command.correlationId());
        SupportTicket savedTicket = ticketRepository.save(ticket);

        return new ReopenTicketResult(savedTicket.getId(), savedTicket.getStatus().name());
    }
}
