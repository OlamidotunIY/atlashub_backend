package com.atlashub.support.application.command.OpenTicket;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.entities.TicketMessage;
import com.atlashub.support.domain.entities.TicketSlaConfig;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import com.atlashub.support.domain.repositories.TicketMessageRepository;
import com.atlashub.support.domain.repositories.TicketSlaConfigRepository;
import com.atlashub.support.domain.valueobject.AuthorType;
import java.time.Year;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class OpenTicketHandler extends Command<OpenTicketCommand, OpenTicketResult> {

    private final SupportTicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final TicketSlaConfigRepository slaConfigRepository;

    public OpenTicketHandler(
            SupportTicketRepository ticketRepository,
            TicketMessageRepository messageRepository,
            TicketSlaConfigRepository slaConfigRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.slaConfigRepository = slaConfigRepository;
    }

    @Override
    public OpenTicketResult execute(OpenTicketCommand command) {
        int firstResponseMinutes = 1440; // Default 24 hours
        int resolutionMinutes = 4320;    // Default 72 hours

        Optional<TicketSlaConfig> slaConfig = slaConfigRepository.findByCategoryAndPriority(
                command.category(), command.priority());
        if (slaConfig.isPresent()) {
            firstResponseMinutes = slaConfig.get().getFirstResponseMinutes();
            resolutionMinutes = slaConfig.get().getResolutionMinutes();
        }

        Long ticketId = ticketRepository.nextIdentity();
        String ticketNumber = "TKT-" + Year.now().getValue() + "-" + String.format("%05d", Math.abs(ticketId % 100000));

        SupportTicket ticket = SupportTicket.create(
                ticketId,
                ticketNumber,
                command.organizationId(),
                command.raisedBy(),
                command.category(),
                command.priority(),
                command.subject(),
                command.relatedEntityType(),
                command.relatedEntityId(),
                firstResponseMinutes,
                resolutionMinutes,
                command.correlationId()
        );

        SupportTicket savedTicket = ticketRepository.save(ticket);

        // Record opening description as first message in thread
        Long messageId = messageRepository.nextIdentity();
        TicketMessage initialMessage = TicketMessage.create(
                messageId,
                savedTicket.getId(),
                command.raisedBy(),
                AuthorType.CUSTOMER,
                command.description(),
                false,
                null
        );
        messageRepository.save(initialMessage);

        return new OpenTicketResult(savedTicket.getId(), savedTicket.getTicketNumber());
    }
}
