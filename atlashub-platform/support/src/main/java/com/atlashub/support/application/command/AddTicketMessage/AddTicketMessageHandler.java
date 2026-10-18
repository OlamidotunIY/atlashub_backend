package com.atlashub.support.application.command.AddTicketMessage;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.entities.TicketMessage;
import com.atlashub.support.domain.events.TicketMessageAddedEvent;
import com.atlashub.support.domain.exceptions.TicketNotFoundException;
import com.atlashub.support.domain.exceptions.UnauthorizedTicketActionException;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import com.atlashub.support.domain.repositories.TicketMessageRepository;
import com.atlashub.support.domain.valueobject.AuthorType;
import com.atlashub.support.domain.valueobject.TicketStatus;
import org.springframework.stereotype.Component;

@Component
public class AddTicketMessageHandler extends Command<AddTicketMessageCommand, AddTicketMessageResult> {

    private final SupportTicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;

    public AddTicketMessageHandler(
            SupportTicketRepository ticketRepository,
            TicketMessageRepository messageRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public AddTicketMessageResult execute(AddTicketMessageCommand command) {
        SupportTicket ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(command.ticketId()));

        if (command.isInternalNote() && command.authorType() == AuthorType.CUSTOMER) {
            throw new UnauthorizedTicketActionException("Customers cannot create internal support notes");
        }

        if (command.authorType() == AuthorType.CUSTOMER && ticket.getStatus() == TicketStatus.PENDING_CUSTOMER_RESPONSE) {
            ticket.resume();
        } else if (command.authorType() == AuthorType.AGENT) {
            ticket.recordFirstResponse();
        }

        Long messageId = messageRepository.nextIdentity();
        TicketMessage message = TicketMessage.create(
                messageId,
                ticket.getId(),
                command.authorId(),
                command.authorType(),
                command.message(),
                command.isInternalNote(),
                command.attachmentUrls()
        );
        TicketMessage savedMessage = messageRepository.save(message);

        ticket.recordMessageAdded(
                command.authorId(),
                command.authorType(),
                command.isInternalNote(),
                command.correlationId()
        );
        ticketRepository.save(ticket);

        return new AddTicketMessageResult(savedMessage.getId(), ticket.getId());
    }
}
