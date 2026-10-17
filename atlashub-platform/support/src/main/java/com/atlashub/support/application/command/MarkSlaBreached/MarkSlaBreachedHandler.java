package com.atlashub.support.application.command.MarkSlaBreached;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.exceptions.TicketNotFoundException;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import org.springframework.stereotype.Component;

@Component
public class MarkSlaBreachedHandler extends Command<MarkSlaBreachedCommand, MarkSlaBreachedResult> {

    private final SupportTicketRepository ticketRepository;

    public MarkSlaBreachedHandler(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public MarkSlaBreachedResult execute(MarkSlaBreachedCommand command) {
        SupportTicket ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(command.ticketId()));

        ticket.markSlaBreached(command.breachType(), command.correlationId());
        SupportTicket savedTicket = ticketRepository.save(ticket);

        return new MarkSlaBreachedResult(savedTicket.getId(), savedTicket.isSlaBreached());
    }
}
