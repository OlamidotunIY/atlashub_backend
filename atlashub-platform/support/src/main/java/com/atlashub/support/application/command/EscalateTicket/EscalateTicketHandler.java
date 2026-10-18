package com.atlashub.support.application.command.EscalateTicket;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.exceptions.TicketNotFoundException;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import org.springframework.stereotype.Component;

@Component
public class EscalateTicketHandler extends Command<EscalateTicketCommand, EscalateTicketResult> {

    private final SupportTicketRepository ticketRepository;

    public EscalateTicketHandler(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public EscalateTicketResult execute(EscalateTicketCommand command) {
        SupportTicket ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(command.ticketId()));

        ticket.escalate(command.newAgentId(), command.escalationReason(), command.correlationId());
        SupportTicket savedTicket = ticketRepository.save(ticket);

        return new EscalateTicketResult(savedTicket.getId(), savedTicket.getAssignedTo());
    }
}
