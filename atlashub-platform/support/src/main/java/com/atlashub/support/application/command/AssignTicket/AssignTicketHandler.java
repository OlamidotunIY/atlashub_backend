package com.atlashub.support.application.command.AssignTicket;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.exceptions.TicketNotFoundException;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import org.springframework.stereotype.Component;

@Component
public class AssignTicketHandler extends Command<AssignTicketCommand, AssignTicketResult> {

    private final SupportTicketRepository ticketRepository;

    public AssignTicketHandler(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public AssignTicketResult execute(AssignTicketCommand command) {
        SupportTicket ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(command.ticketId()));

        ticket.assign(command.agentId(), command.correlationId());
        SupportTicket savedTicket = ticketRepository.save(ticket);

        return new AssignTicketResult(savedTicket.getId(), savedTicket.getAssignedTo(), savedTicket.getStatus().name());
    }
}
