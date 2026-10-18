package com.atlashub.support.application.command.StartTicketProgress;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.exceptions.TicketNotFoundException;
import com.atlashub.support.domain.exceptions.UnauthorizedTicketActionException;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import org.springframework.stereotype.Component;

@Component
public class StartTicketProgressHandler extends Command<StartTicketProgressCommand, StartTicketProgressResult> {

    private final SupportTicketRepository ticketRepository;

    public StartTicketProgressHandler(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public StartTicketProgressResult execute(StartTicketProgressCommand command) {
        SupportTicket ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(command.ticketId()));

        if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().equals(command.agentId())) {
            throw new UnauthorizedTicketActionException("Only assigned agent can start progress on this ticket");
        }

        ticket.startProgress();
        SupportTicket savedTicket = ticketRepository.save(ticket);

        return new StartTicketProgressResult(savedTicket.getId(), savedTicket.getStatus().name());
    }
}
