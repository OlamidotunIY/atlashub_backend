package com.atlashub.support.application.command.ResolveTicket;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.exceptions.TicketNotFoundException;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import org.springframework.stereotype.Component;

@Component
public class ResolveTicketHandler extends Command<ResolveTicketCommand, ResolveTicketResult> {

    private final SupportTicketRepository ticketRepository;

    public ResolveTicketHandler(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public ResolveTicketResult execute(ResolveTicketCommand command) {
        SupportTicket ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(command.ticketId()));

        ticket.resolve(command.agentId(), command.resolutionNote(), command.correlationId());
        SupportTicket savedTicket = ticketRepository.save(ticket);

        return new ResolveTicketResult(
                savedTicket.getId(),
                savedTicket.getStatus().name(),
                savedTicket.getResolutionNote(),
                savedTicket.getResolvedAt()
        );
    }
}
